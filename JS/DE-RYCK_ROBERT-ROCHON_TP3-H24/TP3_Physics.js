const appleMass = 0.075;

TP3.Physics = {
	initTree: function (rootNode) {

		this.computeTreeMass(rootNode);

		var stack = [];
		stack.push(rootNode);

		while (stack.length > 0) {
			var currentNode = stack.pop();
			for (var i = 0; i < currentNode.childNode.length; i++) {
				stack.push(currentNode.childNode[i]);
			}

			currentNode.vel = new THREE.Vector3();
			currentNode.strength = currentNode.a0;
			currentNode.transform = new THREE.Matrix4();
			currentNode.initDir = new THREE.Vector3().subVectors(currentNode.p1, currentNode.p0).normalize();
		}
	},

	computeTreeMass: function (node) {
		var mass = 0;

		for (var i = 0; i < node.childNode.length; i++) {
			mass += this.computeTreeMass(node.childNode[i]);
		}
		mass += node.a1;
		if (node.appleIndices !== null) {
			mass += appleMass;
		}
		node.mass = mass;

		return mass;
	},

	applyForces: function (node, dt, time) {
		//TODO
		var u = Math.sin(1 * time) * 4;
		u += Math.sin(2.5 * time) * 2;
		u += Math.sin(5 * time) * 0.4;

		var v = Math.cos(1 * time + 56485) * 4;
		v += Math.cos(2.5 * time + 56485) * 2;
		v += Math.cos(5 * time + 56485) * 0.4;

		// Ajouter le vent
		node.vel.add(new THREE.Vector3(u/Math.sqrt(node.mass), 0, v/Math.sqrt(node.mass)).multiplyScalar(dt));
		// Ajouter la gravite
		node.vel.add(new THREE.Vector3(0, -node.mass, 0).multiplyScalar(dt));

		// TODO: Projection du mouvement, force de restitution et amortissement de la velocite
		//calcul P1(t+dt)
		var dirVecInit = new THREE.Vector3().subVectors(node.p1, node.p0);
		var originalLength = dirVecInit.length();
		dirVecInit.normalize();

		var newp1 = node.p1.clone().add(node.vel.clone().multiplyScalar(dt));
		var dirVecNew = new THREE.Vector3().subVectors(newp1, node.p0).normalize();

		let axisAngle = TP3.Geometry.findRotation(dirVecInit, dirVecNew);
		let rotMatrix = new THREE.Matrix4().makeRotationAxis(axisAngle[0], axisAngle[1]);

		newp1 = node.p1.clone().applyMatrix4(rotMatrix);
		node.vel = new THREE.Vector3().subVectors(node.p1, newp1).multiplyScalar(dt);

		//calcul restitution
		dirVecNew = new THREE.Vector3().subVectors(newp1, node.p0).normalize();

		axisAngle = TP3.Geometry.findRotation(dirVecInit, dirVecNew);
		rotMatrix = new THREE.Matrix4().makeRotationAxis(axisAngle[0], -Math.pow(axisAngle[1],2));

		node.vel.add(dirVecInit.clone().applyMatrix4(rotMatrix)).multiplyScalar(node.strength * 1000);
		node.vel.multiplyScalar(0.7);

		//calcul nouveau p1 avec la nouvelle vel
		newp1 = node.p1.clone().add(node.vel.clone().multiplyScalar(dt));
		dirVecNew = new THREE.Vector3().subVectors(newp1, node.p0).normalize();

		axisAngle = TP3.Geometry.findRotation(dirVecInit, dirVecNew);
		rotMatrix = new THREE.Matrix4().makeRotationAxis(axisAngle[0], axisAngle[1]);

		node.p1 = node.p0.clone().addScaledVector(dirVecInit.clone().applyMatrix4(rotMatrix), originalLength);
		node.transform = rotMatrix;

		// Appel récursif sur les enfants
		for (let i = 0; i < node.childNode.length; i++) {
			let dirChild = new THREE.Vector3().subVectors(node.childNode[i].p1, node.childNode[i].p0);
			let lengthChild = dirChild.length();
			dirChild.normalize();

			node.childNode[i].p0 = node.p1;

			node.childNode[i].p1 = node.childNode[i].p0.clone().addScaledVector(dirChild.clone().applyMatrix4(rotMatrix), lengthChild);

			this.applyForces(node.childNode[i], dt, time);
		}
	}
}