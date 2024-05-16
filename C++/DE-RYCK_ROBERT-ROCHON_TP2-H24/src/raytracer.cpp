#include "raytracer.h"

void Raytracer::render(const Scene& scene, Frame* output)
{       
    // Créer le z_buffer.
    double *z_buffer = new double[scene.resolution[0] * scene.resolution[1]];
    for(int i = 0; i < scene.resolution[0] * scene.resolution[1]; i++) {
        z_buffer[i] = scene.camera.z_far;
    }

	// @@@@@@ VOTRE CODE ICI
	// Calculez les paramètres de la caméra pour les rayons.

    double3 frontCam = normalize(scene.camera.position - scene.camera.center); // Direction Cam en z
    double3 rightCam = normalize(cross(scene.camera.up,frontCam)); // Direction Cam en x
    double3 upCam = cross(frontCam,rightCam); // Direction Cam en y

    double3 centerPOV = scene.camera.position - scene.camera.z_near * frontCam; // Position centrale du plan

    double heightCam = 2 * scene.camera.z_near * tan(0.5 * deg2rad(scene.camera.fovy)); // Hauteur du plan
    double widthCam = heightCam * scene.camera.aspect; // Largeur du plan

    double pixelWidth = widthCam / scene.resolution[0];  // Largeur d'un pixel basé sur le plan
    double pixelHeight = heightCam / scene.resolution[1]; // Hauteur d'un pixel basé sur le plan

    double3 bottomLeftCam = centerPOV - 0.5 * heightCam * upCam - 0.5 * widthCam * rightCam; // Position du coin gauche en bas du plan

    // Formule Ax + By + Cz + D = 0
    // double D = -1*(dot(frontCam, centerPOV));

    // Itérer sur tous les pixels de l'image.
    for(int y = 0; y < scene.resolution[1]; y++) {
		if (y % 40){
			std::cout << "\rScanlines completed: " << y << "/" << scene.resolution[1] << '\r';
		}

        for(int x = 0; x < scene.resolution[0]; x++) {

			int avg_z_depth = 0;
			double3 avg_ray_color{0,0,0};
			
			for(int iray = 0; iray < scene.samples_per_pixel; iray++) {
				// Générer le rayon approprié pour ce pixel.
				Ray ray;
				// Initialiser la profondeur de récursivité du rayon.
				int ray_depth = 0;
				// Initialiser la couleur du rayon
				double3 ray_color{0,0,0};

				// @@@@@@ VOTRE CODE ICI
				// Mettez en place le rayon primaire en utilisant les paramètres de la caméra.
				// Lancez le rayon de manière uniformément aléatoire à l'intérieur du pixel dans la zone délimité par jitter_radius. 
				// Faites la moyenne des différentes couleurs obtenues suite à la récursion.

                double2 randomValue = rand_double2();
                // Changer la position a une coordonnée aléatoire dans le pixel délimité par jitterRadius
                double3 bottomLeftCamBis = bottomLeftCam +
                        ((scene.jitter_radius + randomValue[0]* (pixelHeight - 2*scene.jitter_radius)) * pixelWidth * rightCam ) +
                        ((scene.jitter_radius + randomValue[1]* (pixelHeight - 2*scene.jitter_radius)) * pixelHeight * upCam);
                double3 pixel = bottomLeftCamBis + pixelWidth * x *rightCam + pixelHeight * y * upCam; //Position du pixel

                ray = Ray(scene.camera.position, normalize((pixel - scene.camera.position)));

                double3* out_color = new double3{0,0,0};
                double* out_z_depth = new double{scene.camera.z_far}; // Max depth basé sur la camera
                trace(scene, ray, ray_depth, out_color, out_z_depth);

                avg_z_depth += *out_z_depth;
                avg_ray_color += *out_color;

                free(out_color); // Déallouer la mémoire
                free(out_z_depth);
            }

            avg_z_depth = avg_z_depth / scene.samples_per_pixel;
            avg_ray_color = avg_ray_color / scene.samples_per_pixel;

            // Test de profondeur
            if(avg_z_depth >= scene.camera.z_near && avg_z_depth <= scene.camera.z_far &&
                avg_z_depth < z_buffer[x + y*scene.resolution[0]]) {
                z_buffer[x + y*scene.resolution[0]] = avg_z_depth;

                // Mettre à jour la couleur de l'image et sa profondeur
                output->set_color_pixel(x, y, avg_ray_color);
                output->set_depth_pixel(x, y, (avg_z_depth - scene.camera.z_near) /
                                        (scene.camera.z_far-scene.camera.z_near));
            }
        }
    }

    delete[] z_buffer;
}

// @@@@@@ VOTRE CODE ICI
// Veuillez remplir les objectifs suivants:
// 		- Détermine si le rayon intersecte la géométrie.
//      	- Calculer la contribution associée à la réflexion.
//			- Calculer la contribution associée à la réfraction.
//			- Mettre à jour la couleur avec le shading +
//			  Ajouter réflexion selon material.reflection +
//			  Ajouter réfraction selon material.refraction
//            pour la couleur de sortie.
//          - Mettre à jour la nouvelle profondeure.
void Raytracer::trace(const Scene& scene,
                      Ray ray, int ray_depth,
                      double3* out_color, double* out_z_depth)
{
    ray_depth++;
    if(ray_depth > 5) { // MAX_DEPTH est une constante définie pour éviter la récursion infinie
        return;
    }

    Intersection hit;
    if(scene.container->intersect(ray, EPSILON, *out_z_depth, &hit)) {
        Material& material = ResourceManager::Instance()->materials[hit.key_material];

        *out_color = shade(scene, hit); // Couleur de base calculée via la méthode de shading
        *out_z_depth = hit.depth;

        // Réflexion
        if(material.k_reflection > 0.0) {
            // Calculer le vecteur de réflexion
            double3 dirRef =2 * (dot(-(ray.direction), hit.normal) * hit.normal) + (ray.direction);
            Ray reflectRay(hit.position + EPSILON * hit.normal, normalize(dirRef));
            double3 reflectColor = {0, 0, 0};
            double reflectDepth = scene.camera.z_far;
            // Calculer la couleur réflective
            trace(scene, reflectRay, ray_depth, &reflectColor, &reflectDepth);
            *out_color += reflectColor * material.k_reflection;
        }

        // Réfraction
        if (material.k_refraction > 0.0) {
            // Calculer le cosinus de l'angle entre le rayon incident et la normale
            double cos1 = dot(-ray.direction, hit.normal);
            double N = 1.0 / material.refractive_index;

            Ray refractRay;

            double cos2 = 1.0 - pow(N, 2) * (1.0 - pow(cos1, 2));

            if (cos2 >= 0.0) {
                double cos2_sqrt = sqrt(cos2);
                // Calculer le vecteur réfraction
                if (cos1 > 0) {
                    refractRay.origin = hit.position - EPSILON * hit.normal;
                    refractRay.direction = normalize(N * ray.direction + (N * cos1 - cos2_sqrt) * hit.normal);
                } else {
                    cos1 = -cos1;
                    refractRay.origin = hit.position + EPSILON * hit.normal;
                    refractRay.direction = normalize(N * ray.direction - (N * cos1 + cos2_sqrt) * -hit.normal);
                }
                // Calculer la couleur réfractive
                double3 refractColor = {0, 0, 0};
                double refractDepth = scene.camera.z_far;
                trace(scene, refractRay, ray_depth, &refractColor, &refractDepth);
                *out_color += refractColor * material.k_refraction;
            }
        }
    }
}

// @@@@@@ VOTRE CODE ICI
// Veuillez remplir les objectifs suivants:
// 		* Calculer la contribution des lumières dans la scène.
//			- Itérer sur toutes les lumières.
//				- Inclure la contribution spéculaire selon le modèle de Blinn en incluant la composante métallique.
//	          	- Inclure la contribution diffuse. (Faites attention au produit scalare. >= 0)
//   	  	- Inclure la contribution ambiante
//      * Calculer si le point est dans l'ombre
//			- Itérer sur tous les objets et détecter si le rayon entre l'intersection et la lumière est occludé.
//				- Ne pas considérer les points plus loins que la lumière.
//			- Par la suite, intégrer la pénombre dans votre calcul
//		* Déterminer la couleur du point d'intersection.
//        	- Si texture est présente, prende la couleur à la coordonnées uv
//			- Si aucune texture, prendre la couleur associé au matériel.

double3 Raytracer::shade(const Scene& scene, Intersection hit)
{
    Material& material = ResourceManager::Instance()->materials[hit.key_material];

    double3 couleurL;
    // Vérifier si une texture est présente et utiliser les coordonnées UV pour obtenir la couleur
    if (material.texture_albedo.width() > 0) {
        unsigned int uv_x = static_cast<unsigned int>(hit.uv[0] * (material.texture_albedo.width() - 1));
        unsigned int uv_y = static_cast<unsigned int>((1.0 - hit.uv[1]) * (material.texture_albedo.height() - 1));

        // Obtenir la couleur de la texture à la position UV, les valeurs de couleur sont retournées en [0..255]
        unsigned char red, green, blue;
        material.texture_albedo.get_pixel(uv_x, uv_y, red, green, blue);

        // Convertir en double et normaliser [0..1]
        couleurL = double3(red / 255.0, green / 255.0, blue / 255.0);
    } else {
        couleurL = material.color_albedo;
    }

    double3 ambientLight = scene.ambient_light * material.k_ambient * couleurL;
    double3 output = ambientLight;

    for (SphericalLight light : scene.lights){

        double3 diffuseLight;
        double3 specularLight;

        double nbRayon = 10;
        double occlusion = 1;
        double t_Light = length(light.position-hit.position);

        if (light.radius <= 0){ // Ombre binaire (ponctuel)
            Intersection shadowHit;
            double3 shadowRayDirection = normalize(light.position - hit.position);
            // Vérifier s'il y a une intersection
            Ray shadowRay(hit.position + hit.normal * EPSILON, shadowRayDirection);
            // Pour un cylindre, on considère que sa partie intérieure n'est pas sous une lumière pour toutes
            // lumières qui intersecte avec la partie extérieure
            if (scene.container->intersect(shadowRay, EPSILON, t_Light, &shadowHit)) {
                continue;
            }
        }
        else{ // Pénombre
            double ombrage = nbRayon;
            for (int x = 0; x < nbRayon; x++){
                // Calculer le disque light
                double3 direction = normalize(hit.position - light.position);
                double3 up = {0.0, 1.0, 0.0};
                double3 u = normalize(cross(up, direction));
                double3 v = cross(direction, u);
                double2 xy = random_in_unit_disk() * light.radius*2 - light.radius;
                // Calculer un point sur le disque
                double3 lightpoint = light.position + u * xy.x + v * xy.y;
                double t_Light = length(lightpoint-hit.position);
                double3 shadowRayDirection = normalize(lightpoint - hit.position);

                // Calculer l'intersection entre un point et le point sur disque
                Intersection shadowHit;
                Ray shadowRay(hit.position + hit.normal * EPSILON, shadowRayDirection);
                if (scene.container->intersect(shadowRay, EPSILON, t_Light, &shadowHit)) {
                    ombrage -= 1;
                }
            }
            occlusion = ombrage/nbRayon;
        }

        double3 L = normalize(light.position-hit.position);
        double scalaireD = dot(hit.normal, L);

        if (scalaireD >= 0){ //(Faire attention au produit scalare. >= 0)
            diffuseLight = material.k_diffuse * scalaireD * couleurL;
        }

        double3 E = normalize(scene.camera.position-hit.position);
        double3 H = normalize(L+E);
        double scalaireS = dot(hit.normal, H);

        if (scalaireS >= 0){ //(Faire attention au produit scalare. >= 0)
            specularLight = material.k_specular * pow(scalaireS, material.shininess)
                    * ((1-material.metallic) + material.metallic*couleurL);
        }

        output += (light.emission/(pow(t_Light, 2)))*(diffuseLight + specularLight) * occlusion;
    }
    return output;
}
