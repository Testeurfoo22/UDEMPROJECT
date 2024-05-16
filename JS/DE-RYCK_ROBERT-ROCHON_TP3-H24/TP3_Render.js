TP3.Render = {
	drawTreeRough: function (rootNode, scene, alpha, radialDivisions = 8, leavesCutoff = 0.1, leavesDensity = 10, applesProbability = 0.05, matrix = new THREE.Matrix4()) {
		//TODO
		//variables de base
		var branchArray = [];
		var leafArray = [];
		var appleArray = [];
		var stack = [];
		stack.push(rootNode);

		while (stack.length > 0) {
			let node = stack.pop();
			node.childNode.forEach(child => stack.push(child));

			//calcul des matrices de transformations en fonctions des branches + creation geometrie branches
			const branch = new THREE.CylinderBufferGeometry(node.a1, node.a0, node.p0.distanceTo(node.p1), radialDivisions);
			const directionVector = new THREE.Vector3().subVectors(node.p1, node.p0).normalize();
			const axisAngle = TP3.Geometry.findRotation(new THREE.Vector3(0, 1, 0), directionVector);
			const rotMatrix = new THREE.Matrix4().makeRotationFromQuaternion(new THREE.Quaternion().setFromAxisAngle(axisAngle[0], axisAngle[1]));

			//ajout de la geometry dans la liste totale
			branch.applyMatrix4(rotMatrix).translate(
				(node.p0.x + node.p1.x) / 2,
				(node.p0.y + node.p1.y) / 2,
				(node.p0.z + node.p1.z) / 2
			);
			branchArray.push(branch);

			//ajout de feuilles
			if (node.a0 < alpha * leavesCutoff) {

				//calcul distance valable
				const long = node.childNode.length === 0 ? alpha : 0;
				const baseDistance = node.p0.distanceTo(node.p1) + long;
				const directionVector = new THREE.Vector3().subVectors(node.p1, node.p0).normalize();

				//vecteur orthogonal sur l'initial
				let perpVector1 = new THREE.Vector3();
				if (Math.abs(directionVector.x) > Math.abs(directionVector.z)) {
					perpVector1.crossVectors(directionVector, new THREE.Vector3(0, 0, 1)).normalize();
				} else {
					perpVector1.crossVectors(directionVector, new THREE.Vector3(1, 0, 0)).normalize();
				}
				let perpVector2 = new THREE.Vector3().crossVectors(directionVector, perpVector1).normalize();

				// creer des feuilles au numbre de leavesDensity avec des position random entre une
				// distance L une rotation Theta et un radius de r
				for (let i = 0; i < leavesDensity; i++) {
					const leaf = new THREE.PlaneBufferGeometry(alpha, alpha);
					var point = node.p0.clone().addScaledVector(directionVector, Math.random() * baseDistance);
					const angle = Math.random() * Math.PI * 2; // Angle aléatoire en radians
					point = point.clone().addScaledVector(perpVector1, Math.cos(angle) * alpha/2).addScaledVector(perpVector2, Math.sin(angle) * alpha/2);

					leaf.rotateX(Math.random() * Math.PI * 2)
						.rotateY(Math.random() * Math.PI * 2)
						.rotateZ(Math.random() * Math.PI * 2)
						.translate(point.x, point.y, point.z);

					leafArray.push(leaf);
				}

				//ajout et creation de pommes de taille alpha3 (alpha/2) pour que le haut soit au contact de la branche
				if (Math.random() <= applesProbability) {
					const apple = new THREE.BoxBufferGeometry(alpha, alpha, alpha).translate(
						(node.p0.x + node.p1.x) / 2,
						((node.p0.y + node.p1.y) / 2) - (alpha / 2),
						(node.p0.z + node.p1.z) / 2
					);
					appleArray.push(apple);
				}
			}
		}

		//aplique uniformement un mesh de tout les geometries (gain de perf)
		var tree, leafs, apples;
		if (branchArray.length){
			let treeGeometry = THREE.BufferGeometryUtils.mergeBufferGeometries(branchArray);
			tree = new THREE.Mesh(treeGeometry, new THREE.MeshLambertMaterial({color: 0x8B5A2B}));
			scene.add(tree);
		}
		if (leafArray.length){
			let leafGeometry = THREE.BufferGeometryUtils.mergeBufferGeometries(leafArray);
			leafs = new THREE.Mesh(leafGeometry, new THREE.MeshPhongMaterial({color: 0x3A5F0B}));
			scene.add(leafs);
		}
		if(appleArray.length){
			let appleGeometry = THREE.BufferGeometryUtils.mergeBufferGeometries(appleArray);
			apples = new THREE.Mesh(appleGeometry, new THREE.MeshPhongMaterial({color: 0x5F0B0B}));
			scene.add(apples);
		}
		return [tree, leafs, apples];
	},

	drawTreeHermite: function (rootNode, scene, alpha, leavesCutoff = 0.1, leavesDensity = 10, applesProbability = 0.05, matrix = new THREE.Matrix4()) {
		//TODO
		//variables de base
		var branchArray = [];
		var leafArray = [];
		var appleArray = [];
		var stack = [rootNode];

		// variables d'inndice et de vertices pour le mesh
		const vertices = [];
		const faceIdx = [];
		var maxIdx = 0; //nombre de idx utilisé
		const lastIdx = [0];//numero d'index a lier (utile pour les branche a plusieurs enfants)


		while (stack.length > 0) {
			let node = stack.pop();
			node.leafIndex = [];
			node.appleIndex = null;

			for (var i = 0; i < node.childNode.length; i++) {
				//if (i === 0/* || i === 1*/){
					stack.push(node.childNode[i]);
				//}
			}

			//creation de mesh en triangle pour le branches avec des liaisons entre 2 segments voisins
			var newIdx = 0;
			let init = (node.parentNode === null) ? 0 : 1;

			for (let x = init; x < node.sections.length; x++) {
				for (let y = 0; y < node.sections[0].length; y++) {
					vertices.push(node.sections[x][y].x, node.sections[x][y].y, node.sections[x][y].z);
					newIdx++;
				}
			}

			let taille = node.sections[0].length;
			let idx = lastIdx.pop();
			idx = idx > 0 ? idx-5 : 0

			for (let x = 0; x < node.sections.length-1; x++) {
				if (x === 0){
					if (node.parentNode === null){
						for (let y = 0; y < taille; y++) {
							let current = y;
							let next = (y + 1) % taille;
							let third = (y + Math.ceil(taille/2)) % taille;

							faceIdx.push(next, current, third);
						}
					}else{
						for (let y = 0; y < taille; y++) {
							let current = y;
							let next = (y + 1) % taille;
							let currentNextSection = y + taille;
							let nextNextSection = (y + 1) % taille + taille;

							faceIdx.push(current + idx, next + idx, currentNextSection + maxIdx-5); // Triangle 1
							faceIdx.push(next + idx, nextNextSection + maxIdx-5, currentNextSection + maxIdx-5); // Triangle 2
						}
						idx = maxIdx-5;
						continue;
					}
				}
				for (let y = 0; y < taille; y++) {
					let current = y + x * taille;
					let next = (y + 1) % taille + x * taille;
					let currentNextSection = y + (x + 1) * taille;
					let nextNextSection = (y + 1) % taille + (x + 1) * taille;

					faceIdx.push(current + idx, next + idx, currentNextSection + idx); // Triangle 1
					faceIdx.push(next + idx, nextNextSection + idx, currentNextSection + idx); // Triangle 2
				}
				if (node.childNode.length === 0){
					for (let y = 0; y < taille; y++) {
						let current = y + (node.sections.length-1) * taille;
						let next = (y + 1) % taille + (node.sections.length-1) * taille;
						let third = (y + Math.ceil(taille/2)) % taille + (node.sections.length-1) * taille;

						faceIdx.push(current + idx, next + idx, third + idx);
					}
				}
			}
			maxIdx += newIdx;

			node.childNode.forEach(child => lastIdx.push(maxIdx));

			//ajout et creation de feuilles
			if (node.a0 < alpha*leavesCutoff){
				//calcul distance valable
				const long = node.childNode.length === 0 ? alpha : 0;
				const baseDistance = node.p0.distanceTo(node.p1) + long;
				const directionVector = new THREE.Vector3().subVectors(node.p1, node.p0).normalize();
				//vecteur orthogonal sur l'initial
				let perpVector1 = new THREE.Vector3();
				if (Math.abs(directionVector.x) > Math.abs(directionVector.z)) {
					perpVector1.crossVectors(directionVector, new THREE.Vector3(0, 0, 1)).normalize();
				} else {
					perpVector1.crossVectors(directionVector, new THREE.Vector3(1, 0, 0)).normalize();
				}
				let perpVector2 = new THREE.Vector3().crossVectors(directionVector, perpVector1).normalize();

				for(let i = 0; i < leavesDensity; i++) {
					//calcul triangle
					const height = Math.sqrt(3) / 2 * alpha;
					let vertices = new Float32Array([
						-alpha, 0, 0,           // Vertex 1: Bas gauche
						alpha, 0, 0,           // Vertex 2: Bas droit
						0, height, 0,              // Vertex 3: Haut
					]);
					var leaf = new THREE.BufferGeometry();
					leaf.setAttribute('position', new THREE.BufferAttribute(vertices, 3));
					leaf.computeVertexNormals(); // Pour un éclairage correct + visible double face

					var point = node.p0.clone().addScaledVector(directionVector, Math.random() * baseDistance);
					const angle = Math.random() * Math.PI * 2; // Angle aléatoire en radians
					point = point.clone().addScaledVector(perpVector1, Math.cos(angle) * alpha/2).addScaledVector(perpVector2, Math.sin(angle) * alpha/2);

					leaf.rotateX(Math.random()*Math.PI*2);
					leaf.rotateY(Math.random()*Math.PI*2);
					leaf.rotateZ(Math.random()*Math.PI*2);

					leaf.translate(point.x, point.y, point.z);
					leafArray.push(leaf);
					node.leafIndex.push(leafArray.length-1);
				}
				//creations de pommes
				if (Math.random() <= applesProbability){
					var apple = new THREE.SphereBufferGeometry(alpha/2, 8, 16);
					apple.translate((node.p0.x + node.p1.x)/2, ((node.p0.y + node.p1.y)/2)-(alpha/2), (node.p0.z + node.p1.z)/2);
					appleArray.push(apple);
					node.appleIndex = appleArray.length;
				}
			}
		}
		const f32vertices = new Float32Array(vertices);
		var branchmesh = new THREE.BufferGeometry();
		branchmesh.setAttribute('position', new THREE.BufferAttribute(f32vertices, 3));
		branchmesh.setIndex(faceIdx);
		branchmesh.computeVertexNormals();
		branchArray.push(branchmesh);

		//application des mesh en 1 mesh global (perf)
		var treeGeometry, leafGeometry, appleGeometry;
		var tree, leafs, apples;
		if (branchArray.length){
			treeGeometry = THREE.BufferGeometryUtils.mergeBufferGeometries(branchArray);
			tree = new THREE.Mesh(treeGeometry, new THREE.MeshLambertMaterial({color: 0x8B5A2B}));
			scene.add(tree);
		}
		if (leafArray.length){
			leafGeometry = THREE.BufferGeometryUtils.mergeBufferGeometries(leafArray);
			leafs = new THREE.Mesh(leafGeometry, new THREE.MeshPhongMaterial({color: 0x3A5F0B, side: THREE.DoubleSide}));
			scene.add(leafs);
		}
		if(appleArray.length){
			appleGeometry = THREE.BufferGeometryUtils.mergeBufferGeometries(appleArray);
			apples = new THREE.Mesh(appleGeometry, new THREE.MeshPhongMaterial({color: 0x5F0B0B}));
			scene.add(apples);
		}
		return [treeGeometry, leafGeometry, appleGeometry];
	},

	updateTreeHermite: function (trunkGeometryBuffer, leavesGeometryBuffer, applesGeometryBuffer, rootNode) {
		//TODO
		var stackTrunk = [];
		stackTrunk.push(rootNode);
		var idx = 0;
		while (stackTrunk.length > 0) {
			var node = stackTrunk.pop();
			for (var i = 0; i < node.childNode.length; i++) {
				stackTrunk.push(node.childNode[i]);
			}
			var init = node.parentNode === null?0:1;
			for (let x = init; x < node.sections.length; x++){
				for (let y = 0; y < node.sections[x].length; y++) {
					var point = new THREE.Vector3(trunkGeometryBuffer[idx], trunkGeometryBuffer[idx+1], trunkGeometryBuffer[idx+2]);
					let dir = new THREE.Vector3().subVectors(point, node.p0);
					let length = dir.length();
					dir.normalize();

					point = node.p0.clone().addScaledVector(dir.clone().applyMatrix4(node.transform), length);

					trunkGeometryBuffer[idx] = point.x;
					trunkGeometryBuffer[idx+1] = point.y;
					trunkGeometryBuffer[idx+2] = point.z;
					idx += 3;
				}
			}
			node.leafIndex.forEach(leafIdx => {
				let index = 9 * (leafIdx);
				for (let x = 0; x < 9; x+=3){
					var point = new THREE.Vector3(leavesGeometryBuffer[index+x], leavesGeometryBuffer[index+x+1], leavesGeometryBuffer[index+x+2]);
					let dir = new THREE.Vector3().subVectors(point, node.p0);
					let length = dir.length();
					dir.normalize();

					point = node.p0.clone().addScaledVector(dir.clone().applyMatrix4(node.transform), length);

					leavesGeometryBuffer[index+x]=(point.x);
					leavesGeometryBuffer[index+x+1]=(point.y);
					leavesGeometryBuffer[index+x+2]=(point.z);
				}
			})
			if (node.appleIndex !== null){
				let index = 459 * (node.appleIndex-1);
				for (let x = 0; x < 459; x+=3){
					var point = new THREE.Vector3(applesGeometryBuffer[index+x], applesGeometryBuffer[index+x+1], applesGeometryBuffer[index+x+2]);
					let dir = new THREE.Vector3().subVectors(point, node.p0);
					let length = dir.length();
					dir.normalize();

					point = node.p0.clone().addScaledVector(dir.clone().applyMatrix4(node.transform), length);

					applesGeometryBuffer[index+x]=(point.x);
					applesGeometryBuffer[index+x+1]=(point.y);
					applesGeometryBuffer[index+x+2]=(point.z);
				}
			}
		}
		/*for (let x = 0; x < leavesGeometryBuffer.length; x+=3){
			if (x%3 === 0){
				rand = highTransform[Math.floor(Math.random() * highTransform.length)]
			}
			var point = new THREE.Vector3(leavesGeometryBuffer[x], leavesGeometryBuffer[x+1], leavesGeometryBuffer[x+2]);
			point.applyMatrix4(rand);
			leavesGeometryBuffer[x]=(point.x);
			leavesGeometryBuffer[x+1]=(point.y);
			leavesGeometryBuffer[x+2]=(point.z);
		}*/
		/*for (let x = 0; x < applesGeometryBuffer.length; x+=3){
			if (x%459 === 0){
				rand = highTransform[Math.floor(Math.random() * highTransform.length)]
			}
			var point = new THREE.Vector3(applesGeometryBuffer[x], applesGeometryBuffer[x+1], applesGeometryBuffer[x+2]);
			point.applyMatrix4(rand);
			applesGeometryBuffer[x]=(point.x);
			applesGeometryBuffer[x+1]=(point.y);
			applesGeometryBuffer[x+2]=(point.z);
		}*/
	},

	drawTreeSkeleton: function (rootNode, scene, color = 0xffffff, matrix = new THREE.Matrix4()) {

		var stack = [];
		stack.push(rootNode);

		var points = [];

		while (stack.length > 0) {
			var currentNode = stack.pop();

			for (var i = 0; i < currentNode.childNode.length; i++) {
				stack.push(currentNode.childNode[i]);
			}

			points.push(currentNode.p0);
			points.push(currentNode.p1);

		}

		var geometry = new THREE.BufferGeometry().setFromPoints(points);
		var material = new THREE.LineBasicMaterial({ color: color });
		var line = new THREE.LineSegments(geometry, material);
		line.applyMatrix4(matrix);
		scene.add(line);

		return line.geometry;
	},

	updateTreeSkeleton: function (geometryBuffer, rootNode) {

		var stack = [];
		stack.push(rootNode);

		var idx = 0;
		while (stack.length > 0) {
			var currentNode = stack.pop();

			for (var i = 0; i < currentNode.childNode.length; i++) {
				stack.push(currentNode.childNode[i]);
			}
			geometryBuffer[idx * 6] = currentNode.p0.x;
			geometryBuffer[idx * 6 + 1] = currentNode.p0.y;
			geometryBuffer[idx * 6 + 2] = currentNode.p0.z;
			geometryBuffer[idx * 6 + 3] = currentNode.p1.x;
			geometryBuffer[idx * 6 + 4] = currentNode.p1.y;
			geometryBuffer[idx * 6 + 5] = currentNode.p1.z;

			idx++;
		}
	},


	drawTreeNodes: function (rootNode, scene, color = 0x00ff00, size = 0.05, matrix = new THREE.Matrix4()) {

		var stack = [];
		stack.push(rootNode);

		var points = [];

		while (stack.length > 0) {
			var currentNode = stack.pop();

			for (var i = 0; i < currentNode.childNode.length; i++) {
				stack.push(currentNode.childNode[i]);
			}

			points.push(currentNode.p0);
			points.push(currentNode.p1);

		}

		var geometry = new THREE.BufferGeometry().setFromPoints(points);
		var material = new THREE.PointsMaterial({ color: color, size: size });
		var points = new THREE.Points(geometry, material);
		points.applyMatrix4(matrix);
		scene.add(points);

	},


	drawTreeSegments: function (rootNode, scene, lineColor = 0xff0000, segmentColor = 0xffffff, orientationColor = 0x00ff00, matrix = new THREE.Matrix4()) {

		var stack = [];
		stack.push(rootNode);

		var points = [];
		var pointsS = [];
		var pointsT = [];

		while (stack.length > 0) {
			var currentNode = stack.pop();

			for (var i = 0; i < currentNode.childNode.length; i++) {
				stack.push(currentNode.childNode[i]);
			}

			const segments = currentNode.sections;
			for (var i = 0; i < segments.length - 1; i++) {
				points.push(TP3.Geometry.meanPoint(segments[i]));
				points.push(TP3.Geometry.meanPoint(segments[i + 1]));
			}
			for (var i = 0; i < segments.length; i++) {
				pointsT.push(TP3.Geometry.meanPoint(segments[i]));
				pointsT.push(segments[i][0]);
			}

			for (var i = 0; i < segments.length; i++) {

				for (var j = 0; j < segments[i].length - 1; j++) {
					pointsS.push(segments[i][j]);
					pointsS.push(segments[i][j + 1]);
				}
				pointsS.push(segments[i][0]);
				pointsS.push(segments[i][segments[i].length - 1]);
			}
		}

		var geometry = new THREE.BufferGeometry().setFromPoints(points);
		var geometryS = new THREE.BufferGeometry().setFromPoints(pointsS);
		var geometryT = new THREE.BufferGeometry().setFromPoints(pointsT);

		var material = new THREE.LineBasicMaterial({ color: lineColor });
		var materialS = new THREE.LineBasicMaterial({ color: segmentColor });
		var materialT = new THREE.LineBasicMaterial({ color: orientationColor });

		var line = new THREE.LineSegments(geometry, material);
		var lineS = new THREE.LineSegments(geometryS, materialS);
		var lineT = new THREE.LineSegments(geometryT, materialT);

		line.applyMatrix4(matrix);
		lineS.applyMatrix4(matrix);
		lineT.applyMatrix4(matrix);

		scene.add(line);
		scene.add(lineS);
		scene.add(lineT);

	}
}