#include "container.h"

// @@@@@@ VOTRE CODE ICI
// - Parcourir l'arbre DEPTH FIRST SEARCH selon les conditions suivantes:
// 		- S'il s'agit d'une feuille, faites l'intersection avec la géométrie.
//		- Sinon, il s'agit d'un noeud altérieur.
//			- Faites l'intersection du rayon avec le AABB gauche et droite. 
//				- S'il y a intersection, ajouter le noeud à ceux à visiter. 
// - Retourner l'intersection avec la profondeur maximale la plus PETITE.
bool BVH::intersect(Ray ray, double t_min, double t_max, Intersection* hit) {
    std::vector<BVHNode*> nodesToVisit;
    nodesToVisit.push_back(root);
    bool check = false;
    double closestSoFar = t_max; // Utiliser une variable locale pour garder la trace de l'intersection la plus proche

    while (!nodesToVisit.empty()) {
        BVHNode* currentNode = nodesToVisit.back();
        nodesToVisit.pop_back();

        // Si le nœud courant n'intersecte pas, passez au suivant
        if (!currentNode->aabb.intersect(ray, t_min, closestSoFar)) continue;

        if (currentNode->left == nullptr && currentNode->right == nullptr) {
            // C'est une feuille, pas d'enfants
            Object* object = objects[currentNode->idx];
            // Ici, pas besoin de vérifier à nouveau l'intersection avec l'AABB
            if (object->intersect(ray, t_min, closestSoFar, hit)) {
                check = true;
                closestSoFar = hit->depth; // Mettez à jour la profondeur la plus proche
            }
        } else {
            // C'est un nœud interne, ajoutez les enfants à la pile de visite
            if (currentNode->left != nullptr) nodesToVisit.push_back(currentNode->left);
            if (currentNode->right != nullptr) nodesToVisit.push_back(currentNode->right);
        }
    }

    return check;
}

// @@@@@@ VOTRE CODE ICI
// - Parcourir tous les objets
// 		- Détecter l'intersection avec l'AABB
//			- Si intersection, détecter l'intersection avec la géométrie.
//				- Si intersection, mettre à jour les paramètres.
// - Retourner l'intersection avec la profondeur maximale la plus PETITE.
bool Naive::intersect(Ray ray, double t_min, double t_max, Intersection* hit) {
    bool check = false;
    double closestSoFar = t_max; // Utiliser une variable locale pour garder la trace de l'intersection la plus proche
    // Parcourir tous les "objects"
    for (size_t i = 0; i < objects.size(); ++i) {
        Object* object = objects[i];
        AABB& aabb = aabbs[i];
        // Vérifier l'intersection sur aabb. Si "true" => sur l'objet
        if ((aabb.intersect(ray, t_min, closestSoFar)) && (object->intersect(ray, t_min, closestSoFar, hit))){
            check = true;
            closestSoFar = hit->depth; // Mettre à jour la profondeur la plus proche
        }
    }
	return check;
}
