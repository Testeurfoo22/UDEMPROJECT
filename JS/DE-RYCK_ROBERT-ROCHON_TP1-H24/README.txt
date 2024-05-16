Équipe
******
Hadrien De Ryck
Étienne Robert-Rochon


Date
****
08/02/2024


Structure du personnage
***********************
Torso/
├─ Head/
├─ Arms/
│  ├─ Forearms/
├─ Thighs/
│  ├─ Legs/


Matrices
********
Les matrices (translation, scaling, rotation, etc.) ont été pris des notes de cours.


Robot
*****
Les paramètres suivants ont été ajoutés dans le constructeur pour:
- this.ThighAngleAnim
	Valeur représentant l'angle du "thigh" droit
- this.halfWalk
	Boolean représentant le "thigh" droit qui avance si "true" et recule si "false
-this.left(right)Forearm(leg)Angle
    garde en memoire la rotation des forearms et legs

La fonction:
- updateWalk() s'occupe de la rotation des "thighs" lorsque le personnage doit se déplacer.
- animationWalk(speed) s'occupe de garder le personnage coller au plan.

Les "thighs", "legs", "arms" et "forarms" sont réprésentés graphiquement avec la même sphère nommée "armGeometry".


Head
****
Rotation sur une (1) axe avec 'A' et 'D'


Torso
*****
Rotation sur une (1) axe  avec 'A' et 'D'
Déplacement sur l'orientation choisie avec 'W' et 'S'
changement de move/rotateTorso pour faire suivre les autres parties du corps


Arm
***
Rotation sur trois (2) axes avec 'W', 'A', 'S' et 'D'


Forearm
*******
Rotation sur une (1) axe avec 'W' et 'S'

- rotateForearm(angle, side, bool);
	bool permet de faire la difference entre un movement VOLONTAIRE d'un forearm,
	 et permet de changer (true), ou non (false) la valeur de left(right)ForearmAngle
	  avec ce bool, on peut faire des rotations temporaires, et garder la position initiale dans left(right)ForearmAngle


Thigh
*****
Rotation sur une (1) axe avec 'W' et 'S'


Leg
***
Rotation sur une (1) axe avec 'W' et 'S'

- rotateLeg(angle, side, bool);
	bool permet de faire la difference entre un movement VOLONTAIRE d'un forearm,
	 et permet de changer (true), ou non (false) la valeur de left(right)ForearmAngle
	  avec ce bool, on peut faire des rotations temporaires, et garder la position initiale dans left(right)LegAngle