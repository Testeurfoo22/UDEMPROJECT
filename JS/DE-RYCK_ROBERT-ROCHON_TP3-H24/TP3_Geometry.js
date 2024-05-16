
class Node {
	constructor(parentNode) {
		this.parentNode = parentNode; //Noeud parent
		this.childNode = []; //Noeud enfants

		this.p0 = null; //Position de depart de la branche
		this.p1 = null; //Position finale de la branche

		this.a0 = null; //Rayon de la branche a p0
		this.a1 = null; //Rayon de la branche a p1

		this.sections = null; //Liste contenant une liste de points representant les segments circulaires du cylindre generalise
	}
}

TP3.Geometry = {

	simplifySkeleton: function (rootNode, rotationThreshold = 0.0001) {
		//TODO
		//fin branche
		if (!rootNode.childNode || rootNode.childNode.length === 0) {
			return rootNode;
		}

		if (rootNode.childNode.length === 1) {
			const child = rootNode.childNode[0];
			const parentVector = new THREE.Vector3().subVectors(rootNode.p1, rootNode.p0);
			const childVector = new THREE.Vector3().subVectors(child.p1, child.p0);
			const angle = this.findRotation(parentVector, childVector)[1];
			//si angle, on coupe le point enfant
			if (angle < rotationThreshold) {
				rootNode.a1 = child.a1;
				rootNode.p1 = child.p1;
				rootNode.childNode = child.childNode;
				// update pour tout les nouveaux enfants
				rootNode.childNode.forEach(childNode => {
					childNode.a0 = rootNode.a1;
					childNode.p0 = rootNode.p1;
					childNode.parentNode = rootNode;
				});
				return this.simplifySkeleton(rootNode);
			}
		}

		//recurssion sur les noeds suivants
		rootNode.childNode = rootNode.childNode.map(child => this.simplifySkeleton(child));

		return rootNode;
	},

	generateSegmentsHermite: function (rootNode, lengthDivisions = 4, radialDivisions = 8) {
		//TODO
		let stack = [rootNode];
		var index = 0;
		while (stack.length > 0) {
			var node = stack.pop();
			for (var i = 0; i < node.childNode.length; i++) {
				//if (i === 0/* || i === 1*/){
					stack.push(node.childNode[i]);
				//}
			}
			node.sections = new Array(lengthDivisions); //Initialisation de la liste des sections
			let pointsSection = new Array(lengthDivisions); //Liste de tous les (p,v) des segments
			node.sectionsIndex = new Array(lengthDivisions);

			//position initiales des courbes entre p0 et p1
			var v0 = new THREE.Vector3().subVectors(node.p1,node.p0);
			if(node.parentNode != null){
				v0 = new THREE.Vector3().subVectors(node.parentNode.p1,node.parentNode.p0);
			}
			var v1 = new THREE.Vector3().subVectors(node.p1,node.p0);

			//loop sur les x section creer par lengthDivisions
			for(let i= 0; i < node.sections.length; i++){
				//creation de la courbe de bezier entre v0 et v1 avec p0 et p1
				pointsSection[i] = this.hermite(node.p0, node.p1, v0, v1, i/(lengthDivisions-1));
				let pas = node.a1/node.a0;

				let pointsSectionIndex = new Array(lengthDivisions);
				for (let j = 0; j < radialDivisions; j++) {
					pointsSectionIndex[j] = index + j;
				}

				//cas pour le premier segment, si racine en calcul nos points sur le premier segment sinon on prends
				// le dernier segment de la branche inferieur
				if(i === 0){
					if(node.parentNode == null){
						let D = -pointsSection[i][1].dot(pointsSection[i][0]);
						let y = -(pointsSection[i][1].x * 2 + pointsSection[i][1].z + D) / pointsSection[i][1].y; // Simplified multiplication by 1
						let X = new THREE.Vector3(2.0, y, 1.0).normalize();
						let Z = new THREE.Vector3().crossVectors(pointsSection[i][1], X).normalize();

						node.sections[i] = new Array(radialDivisions);

						for (let j = 0; j < radialDivisions; j++) {
							const angle = j * (-2 * Math.PI / radialDivisions);
							node.sections[i][j] = pointsSection[i][0].clone()
								.addScaledVector(X, node.a0 * Math.cos(angle))
								.addScaledVector(Z, node.a0 * Math.sin(angle));
						}
						node.sectionsIndex[i] = pointsSectionIndex;
						index += radialDivisions;
					} else{
						node.sections[i] = node.parentNode.sections[lengthDivisions-1];
						node.sectionsIndex[i] = node.parentNode.sectionsIndex[lengthDivisions-1];
					}
					// sinon on calcule les points sur chaque segment entre
				} else{
					node.sections[i] = new Array(radialDivisions);

					let axisAngle = this.findRotation(pointsSection[i-1][1], pointsSection[i][1]);
					let rotMat = new THREE.Matrix4().makeRotationFromQuaternion(
						new THREE.Quaternion().setFromAxisAngle(axisAngle[0],axisAngle[1]));
					let currentRadiusValue = node.a0 * (1 + (i / (lengthDivisions-1) * (pas-1)));

					for(let j=0; j<radialDivisions; j++){
						node.sections[i][j] = node.sections[i - 1][j].clone().add(
							new THREE.Vector3().subVectors(pointsSection[i][0], pointsSection[i - 1][0])
						);

						let currentRadius = new THREE.Vector3().subVectors(node.sections[i][j], pointsSection[i][0])
							.normalize().applyMatrix4(rotMat);

						node.sections[i][j] = pointsSection[i][0].clone().addScaledVector(currentRadius, currentRadiusValue);
					}
					node.sectionsIndex[i] = pointsSectionIndex;
					index += radialDivisions;
				}
			}
		}
		return rootNode;
	},

	hermite: function (h0, h1, v0, v1, t) {
		//TODO
		let bezierM = new THREE.Matrix4();
		let hermiteM = new THREE.Matrix4();

		bezierM.set(
			3, 0, 0, 0,
			3, 0, 1, 0,
			0, 3, 0, -1,
			0, 3, 0, 0
		);
		hermiteM.set(
			h0.x, h0.y, h0.z, 0,
			h1.x, h1.y, h1.z, 0,
			v0.x, v0.y, v0.z, 0,
			v1.x, v1.y, v1.z, 0
		);
		bezierM.multiplyMatrices(bezierM, hermiteM)
		bezierM.multiplyScalar(1/3);
		let pArray = [];
		for (let i = 0; i < 4; i++) {
			// Extraire les éléments de chaque ligne, en ignorant le 4ème élément
			let x = bezierM.elements[i];
			let y = bezierM.elements[i + 4];
			let z = bezierM.elements[i + 8];
			pArray.push(new THREE.Vector3(x, y, z));
		}
		while(pArray.length != 2){
			for (var i = 0; i < (pArray.length-1); i++) {
				pArray[i].multiplyScalar(1 - t).addScaledVector(pArray[i + 1], t);
			}
			pArray.pop();
		}
		const p = pArray[0].clone().multiplyScalar(1 - t).addScaledVector(pArray[1], t);
		const dp = (pArray[0].clone().sub(pArray[1])).normalize();
		return [p,dp];
	},


	// Trouver l'axe et l'angle de rotation entre deux vecteurs
	findRotation: function (a, b) {
		const axis = new THREE.Vector3().crossVectors(a, b).normalize();
		var c = a.dot(b) / (a.length() * b.length());

		if (c < -1) {
			c = -1;
		} else if (c > 1) {
			c = 1;
		}

		const angle = Math.acos(c);

		return [axis, angle];
	},

	// Projeter un vecter a sur b
	project: function (a, b) {
		return b.clone().multiplyScalar(a.dot(b) / (b.lengthSq()));
	},

	// Trouver le vecteur moyen d'une liste de vecteurs
	meanPoint: function (points) {
		var mp = new THREE.Vector3();

		for (var i = 0; i < points.length; i++) {
			mp.add(points[i]);
		}

		return mp.divideScalar(points.length);
	},
};