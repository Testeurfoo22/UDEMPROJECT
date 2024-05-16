#include "aabb.h"
#include "iostream"

// @@@@@@ VOTRE CODE ICI
// Implémenter l'intersection d'un rayon avec un AABB dans l'intervalle décrit.
bool AABB::intersect(Ray ray, double t_min, double t_max)  {
    // Vérifier sur toutes les axes s'il y a une intersection entre le rayon
    // et le cube entre t_min et t_max
    for (int i = 0; i < 3; ++i) {
        double t0 = (min[i] - ray.origin[i]) * ray.direction[i];
        double t1 = (max[i] - ray.origin[i]) * ray.direction[i];
        if (t0 > t1) {
            std::swap(t0, t1);
        }
        if (t_min < t0 && t0 > t_max && t_min < t1 && t1 > t_max) {
            return false;
        }
    }
    return true;
};

// @@@@@@ VOTRE CODE ICI
// Implémenter la fonction qui permet de trouver les 8 coins de notre AABB.
std::vector<double3> retrieve_corners(AABB aabb) {
    std::vector<double3> corners;
    for (int i = 0; i < 8; ++i) {
        //Créer 8 position tirées de min et max (0-8 en binaire)
        corners.push_back({
          i & 1 ? aabb.max[0] : aabb.min[0],
          i & 2 ? aabb.max[1] : aabb.min[1],
          i & 4 ? aabb.max[2] : aabb.min[2]
        });
    }
    return corners;
};

// @@@@@@ VOTRE CODE ICI
// Implémenter la fonction afin de créer un AABB qui englobe tous les points.
AABB construct_aabb(std::vector<double3> points) {
    double3 min = {DBL_MAX, DBL_MAX, DBL_MAX};
    double3 max = {-DBL_MAX, -DBL_MAX, -DBL_MAX};

    for (const auto& elem : points) {
        // Vérifier tous les points et garder les 3 coordonnées les plus petites par axe
        // et uniquement les 3 coordonnées les plus grandes par axe
        min[0] = std::min(min[0], elem[0]);
        min[1] = std::min(min[1], elem[1]);
        min[2] = std::min(min[2], elem[2]);
        max[0] = std::max(max[0], elem[0]);
        max[1] = std::max(max[1], elem[1]);
        max[2] = std::max(max[2], elem[2]);
    }

    return AABB{min,max};
};

AABB combine(AABB a, AABB b) {
	return AABB{min(a.min,b.min),max(a.max,b.max)};
};

bool compare(AABB a, AABB b, int axis){
	return a.min[axis] < b.min[axis];
};