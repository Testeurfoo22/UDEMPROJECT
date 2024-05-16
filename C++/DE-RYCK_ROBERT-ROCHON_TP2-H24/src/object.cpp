#include "object.h"

// Fonction retournant soit la valeur v0 ou v1 selon le signe.
int rsign(double value, double v0, double v1) {
	return (int(std::signbit(value)) * (v1-v0)) + v0;
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de trouver l'intersection d'une sphère.
//
// Référez-vous au PDF pour la paramétrisation des coordonnées UV.
//
// Pour plus de d'informations sur la géométrie, référez-vous à la classe object.h.
bool Sphere::local_intersect(Ray ray, 
							 double t_min, double t_max, 
							 Intersection *hit) 
{
    // La formule pour l'intersection d'une sphère peut être représenté par:
    // At^2+Bt+C = 0
    double a = dot(ray.direction, ray.direction);
    double b = 2 * dot(ray.origin, ray.direction);
    double c = dot(ray.origin, ray.origin) - pow(radius, 2);

    double delta = pow(b, 2) - 4*a*c;
    // Si delta < 0 => pas d'intersection
    // Si delta = 0 => 1 intersection
    // Si delta > 0 => 2 intersections
    if (delta < 0){
        return false;
    }
    else {
        // Formule quadratique, si d = 0, t1 et t2 equivalent
        double t1 = ((-b) - sqrt(delta)) / (2 * a);
        double t2 = ((-b) + sqrt(delta)) / (2 * a);
        double t;

        if (t1 > 0 && t1 < t2){
            t = t1;
        }
        else if (t2 > 0){
            t = t2;
        }
        else{
            return false;
        }
        // t > 0 champ de vision
        if(t > t_min && t <= hit->depth && t < t_max){
            // Mettre à jour les variables
            hit->depth = t;
            hit->position = ray.origin + ray.direction * t;
            hit->normal = normalize(hit->position);
            hit->uv = {1-((atan2(hit->position[0], -hit->position[2])/(2*PI)) + 0.5),
                       1-(0.5-((asin(hit->position[1]/radius))/PI))};
            // Vérifier
            if (hit->uv[0] < 0 || hit->uv[0] > 1 || hit->uv[1] < 0 || hit->uv[1] > 1){
                std::cout << "SPHERE" << std::endl;
            }
            return true;
        }
    }

    return false;
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de calculer le AABB pour la sphère.
// Il faut que le AABB englobe minimalement notre objet à moins que l'énoncé prononce le contraire (comme ici).
AABB Sphere::compute_aabb() {
    std::vector<double3> localPoints;
    // Créer un cube recouvrant la sphere
    for (int i = 0; i < 8; ++i) {
        localPoints.push_back({
              i & 1 ? radius : -radius,
              i & 2 ? radius : -radius,
              i & 4 ? radius : -radius
                          });
    }
    // Transformer les coins de l'AABB local en points dans l'espace global
    for (int i = 0; i < localPoints.size(); i++) {
        localPoints[i] = (mul(transform, {localPoints[i], 1}).xyz());
    }
    // Reconstruire l'AABB à partir des points dans l'espace global
    return construct_aabb(localPoints);
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de trouver l'intersection avec un quad (rectangle).
//
// Référez-vous au PDF pour la paramétrisation des coordonnées UV.
//
// Pour plus de d'informations sur la géométrie, référez-vous à la classe object.h.
bool Quad::local_intersect(Ray ray, 
							double t_min, double t_max, 
							Intersection *hit)
{
    // Vecteur normal Z+
    double3 normale = {0, 0, 1};

    double denom = dot(ray.direction,normale);
    if (denom == 0){ // Vérifier la parallele
        return false;
    }
    // Calculer l'intersection sur un plan (un rectangle est plusieurs plans)
    double t = -(dot( ray.origin,normale)) / denom;
    double3 intersec = ray.origin + t*ray.direction;

    if (-half_size < intersec[0] && intersec[0] < half_size &&
            -half_size < intersec[1] && intersec[1] < half_size){
        // t > 0 champ de vision
        if(t > t_min && t <= hit->depth && t <= t_max){
            // Mettre à jour les variables
            hit->depth = t;
            hit->position = intersec;
            double3 normaleN = {0, 0, ray.origin[2] - hit->position[2]};
            hit->normal = normalize(normaleN);
            hit->uv = {(hit->position[0]+half_size)/(2*half_size), (hit->position[1]+half_size)/(2*half_size)};
            // Vérifier
            if (hit->uv[0] < 0 || hit->uv[0] > 1 || hit->uv[1] < 0 || hit->uv[1] > 1){
                std::cout << "QUAD" << std::endl;
            }
            return true;
        }
    }
    return false;
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de calculer le AABB pour le quad (rectangle).
// Il faut que le AABB englobe minimalement notre objet à moins que l'énoncé prononce le contraire.
AABB Quad::compute_aabb() {
    std::vector<double3> localPoints;
    // Créer un cube recouvrant le Quad
    for (int i = 0; i < 8; ++i) {
        localPoints.push_back({
              i & 1 ? half_size : -half_size,
              i & 2 ? half_size : -half_size,
              i & 4 ? EPSILON : -EPSILON
                              });
    }
    // Transformer les coins de l'AABB local en points dans l'espace global
    for (int i = 0; i < localPoints.size(); i++) {
        localPoints[i] = (mul(transform, {localPoints[i], 1}).xyz());
    }

    // Reconstruire l'AABB à partir des points dans l'espace global
    return construct_aabb(localPoints);
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de trouver l'intersection avec un cylindre.
//
// Référez-vous au PDF pour la paramétrisation des coordonnées UV.
//
// Pour plus de d'informations sur la géométrie, référez-vous à la classe object.h.
bool Cylinder::local_intersect(Ray ray, 
							   double t_min, double t_max, 
							   Intersection *hit)
{
    bool inner = false;
    // Même principe que le l'intersection d'une sphère avec la formule quadratique
    // sauf les valeurs de a, b, c changent
    double a = pow(ray.direction[0], 2) + pow(ray.direction[2], 2);
    double b = 2 * (ray.origin[0]*ray.direction[0] + ray.origin[2]*ray.direction[2]);
    double c = pow(ray.origin[0], 2) + pow(ray.origin[2], 2) - pow(radius, 2);

    double delta = pow(b, 2) - 4*a*c;

    if (delta < 0){
        return false;
    }
    else {
        // ATTENTION le cylindre est vide donc afficher si besoin les parties arrières
        double t1 = ((-b) - sqrt(delta)) / (2 * a);
        double t2 = ((-b) + sqrt(delta)) / (2 * a);
        double t;

        double3 intersec1 = ray.origin + t1 * ray.direction;
        double3 intersec2 = ray.origin + t2 * ray.direction;
        double3 intersec;

        // Vérifier le point le plus proche sachant que le cylindre est vide aux extremité
        if (t1 > 0 && intersec1[1] <= half_height && intersec1[1] >= -half_height){
            intersec = ray.origin + t1 * ray.direction;
            t = t1;
        }
        else if (t2 > 0 && intersec2[1] <= half_height && intersec2[1] >= -half_height){
            intersec = ray.origin + t2 * ray.direction;
            t = t2;
            inner = true;
        }
        else{
            return false;
        }

        if (-half_height < intersec[1] && intersec[1] < half_height){
            // t > 0 champ de vision
            if(t > t_min && t <= hit->depth && t < t_max){
                // Mettre à jour les variables
                hit->depth = t;
                hit->position = intersec;
                double3 centerY = {0, hit->position[1], 0};
                hit->normal = inner ? normalize(centerY - hit->position) : normalize(hit->position - centerY);
                double mod = 1.0;
                // Utilisation d'un modulo pour déplacer les coordonnées de 0.25 (pour resssembler aux reférences)
                hit->uv = {modf((atan2(hit->position[0], hit->position[2])/(2*PI)) + 0.75, &mod),
                           (hit->position[1]+half_height)/(2*half_height)};
                // Vérifier
                if (hit->uv[0] < 0 || hit->uv[0] > 1 || hit->uv[1] < 0 || hit->uv[1] > 1) {
                    std::cout << "CYLINDRE" << std::endl;
                }
                return true;
            }
        }
    }

    return false;
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de calculer le AABB pour le cylindre.
// Il faut que le AABB englobe minimalement notre objet à moins que l'énoncé prononce le contraire (comme ici).
AABB Cylinder::compute_aabb() {
    std::vector<double3> localPoints;
    //creation cube recouvrant le cylindre
    for (int i = 0; i < 8; ++i) {
        localPoints.push_back({
          i & 1 ? radius : -radius,
          i & 2 ? half_height : -half_height,
          i & 4 ? radius : -radius
                              });
    }
    /**AABB localAabb = construct_aabb(localPoints);
    // Transformer les coins de l'AABB local en points dans l'espace global
    std::vector<double3> global_points = retrieve_corners(localAabb);
    for (int i = 0; i < global_points.size(); i++) {
        global_points[i] = (mul(transform, {global_points[i], 1}).xyz());
    }

    // Reconstruire l'AABB à partir des points dans l'espace global
    return construct_aabb(global_points);**/
    // Transformer les coins de l'AABB local en points dans l'espace global
    for (int i = 0; i < localPoints.size(); i++) {
        localPoints[i] = (mul(transform, {localPoints[i], 1}).xyz());
    }

    // Reconstruire l'AABB à partir des points dans l'espace global
    return construct_aabb(localPoints);
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de trouver l'intersection avec un mesh.
//
// Référez-vous au PDF pour la paramétrisation pour les coordonnées UV.
//
// Pour plus de d'informations sur la géométrie, référez-vous à la classe object.h.
//
bool Mesh::local_intersect(Ray ray,  
						   double t_min, double t_max, 
						   Intersection* hit)
{
    //permet de garder la prodonfeur la plus courte
    bool check = false;
    double closestSoFar = t_max; // Utilisez une variable locale pour garder la trace de l'intersection la plus proche

    for (Triangle tri : triangles){
        if (intersect_triangle(ray, t_min, closestSoFar, tri,  hit)){
            //si une intersection est plus proche, on la garde
            check = true;
            closestSoFar = hit->depth; // Mettez à jour la profondeur la plus proche
        }
    }
	return check;
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de trouver l'intersection avec un triangle.
// S'il y a intersection, remplissez hit avec l'information sur la normale et les coordonnées texture.
bool Mesh::intersect_triangle(Ray  ray, 
							  double t_min, double t_max,
							  Triangle const tri,
							  Intersection *hit)
{
	// Extrait chaque position de sommet des données du maillage.
	double3 const &p0 = positions[tri[0].pi]; // ou Sommet A (Pour faciliter les explications)
	double3 const &p1 = positions[tri[1].pi]; // ou Sommet B
	double3 const &p2 = positions[tri[2].pi]; // ou Sommet C

	// Triangle en question. Respectez la convention suivante pour vos variables.
	//
	//     A
	//    / \
	//   /   \
	//  B --> C
	//
	// Respectez la règle de la main droite pour la normale.

	// @@@@@@ VOTRE CODE ICI
	// Décidez si le rayon intersecte le triangle (p0,p1,p2).
	// Si c'est le cas, remplissez la structure hit avec les informations
	// de l'intersection et renvoyez true.
	// Pour plus de d'informations sur la géométrie, référez-vous à la classe dans object.hpp.
	//
	// NOTE : hit.depth est la profondeur de l'intersection actuellement la plus proche,
	// donc n'acceptez pas les intersections qui occurent plus loin que cette valeur.

    //TIRE DE L'ALGO DE Möller-Trumbore
    //https://fr.wikipedia.org/wiki/Algorithme_d%27intersection_de_M%C3%B6ller-Trumbore
    double3 normale = cross(ray.direction, p2 - p0);

    double det = dot(p1 - p0,normale);
    if (det > -EPSILON && det < EPSILON){ // check parallele
        return false;
    }

    double3 T = ray.origin - p0;
    double b1 = dot(T,normale) / det;

    double3 Q = cross(T,p1-p0);
    double b2 = dot(ray.direction,Q) / det;
    if (b2 < 0.0 || b1 < 0.0 || b1 + b2 > 1.0){
        return false;
    }

    // On calcule t pour savoir ou le point d'intersection se situe sur la ligne.
    double t = dot(p2-p0,Q) / det;

    //t>0 champ de vision
    if(t >= t_min && t <= hit->depth && t <= t_max){
        //mise a jour des variables avec les interpolations voulu
        hit->depth = t;
        hit->position = ray.origin + ray.direction * t;
        double w = 1.0 - b1 - b2; // coordonnée barycentrique
        double3 normal0 = normals[tri[0].ni];
        double3 normal1 = normals[tri[1].ni];
        double3 normal2 = normals[tri[2].ni];
        hit->normal = normalize(w * normal0 + b1 * normal1 + b2 * normal2);
        double2 tex0 = tex_coords[tri[0].ti];
        double2 tex1 = tex_coords[tri[1].ti];
        double2 tex2 = tex_coords[tri[2].ti];
        hit->uv = w * tex0 + b1 * tex1 + b2 * tex2;
        hit->uv[1] = 1 - hit->uv[1];
        //checker
        if (hit->uv[0] < 0 || hit->uv[0] > 1 || hit->uv[1] < 0 || hit->uv[1] > 1){
            std::cout << "MESH" << std::endl;
        }
        return true;
    }
    return false;
}

// @@@@@@ VOTRE CODE ICI
// Occupez-vous de compléter cette fonction afin de calculer le AABB pour le Mesh.
// Il faut que le AABB englobe minimalement notre objet à moins que l'énoncé prononce le contraire.
AABB Mesh::compute_aabb() {
    std::vector<double3> localPoints;
    //creation cubes recouvrant chaque triangle (push tout les corners possibles)
    for (Triangle tri : triangles){
        double3 const &p0 = positions[tri[0].pi];
        double3 const &p1 = positions[tri[1].pi];
        double3 const &p2 = positions[tri[2].pi];

        double2 x = {std::min({p0[0], p1[0], p2[0]}), std::max({p0[0], p1[0], p2[0]})};
        double2 y = {std::min({p0[1], p1[1], p2[1]}), std::max({p0[1], p1[1], p2[1]})};
        double2 z = {std::min({p0[2], p1[2], p2[2]}), std::max({p0[2], p1[2], p2[2]})};
        for (int i = 0; i < 8; ++i) {
            localPoints.push_back({
                  i & 1 ? x[0] : x[1],
                  i & 2 ? y[0] : y[1],
                  i & 4 ? z[0] : z[1],
                                  });
        }
    }
    /**AABB localAabb = construct_aabb(localPoints);
    // Transformer les coins de l'AABB local en points dans l'espace global
    std::vector<double3> global_points = retrieve_corners(localAabb);
    for (int i = 0; i < global_points.size(); i++) {
        global_points[i] = (mul(transform, {global_points[i], 1}).xyz());
    }

    // Reconstruire l'AABB à partir des points dans l'espace global
    return construct_aabb(global_points);**/
    // Transformer les coins de l'AABB local en points dans l'espace global
    for (int i = 0; i < localPoints.size(); i++) {
        localPoints[i] = (mul(transform, {localPoints[i], 1}).xyz());
    }

    // Reconstruire l'AABB à partir des points dans l'espace global
    return construct_aabb(localPoints);
}