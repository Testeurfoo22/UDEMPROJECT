// ASSIGNMENT-SPECIFIC API EXTENSION
THREE.Object3D.prototype.setMatrix = function(a) {
  this.matrix = a;
  this.matrix.decompose(this.position, this.quaternion, this.scale);
};

var start = Date.now();
// SETUP RENDERER AND SCENE
var scene = new THREE.Scene();
var renderer = new THREE.WebGLRenderer();
renderer.setClearColor(0xffffff); // white background colour
document.body.appendChild(renderer.domElement);

// SETUP CAMERA
var camera = new THREE.PerspectiveCamera(30, 1, 0.1, 1000); // view angle, aspect ratio, near, far
camera.position.set(10,5,10);
camera.lookAt(scene.position);
scene.add(camera);

// SETUP ORBIT CONTROL OF THE CAMERA
var controls = new THREE.OrbitControls(camera);
controls.damping = 0.2;

// ADAPT TO WINDOW RESIZE
function resize() {
  renderer.setSize(window.innerWidth, window.innerHeight);
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
}

window.addEventListener('resize', resize);
resize();

// FLOOR WITH CHECKERBOARD
var floorTexture = new THREE.ImageUtils.loadTexture('images/tile.jpg');
floorTexture.wrapS = floorTexture.wrapT = THREE.MirroredRepeatWrapping;
floorTexture.repeat.set(4, 4);

var floorMaterial = new THREE.MeshBasicMaterial({ map: floorTexture, side: THREE.DoubleSide });
var floorGeometry = new THREE.PlaneBufferGeometry(15, 15);
var floor = new THREE.Mesh(floorGeometry, floorMaterial);
floor.rotation.x = Math.PI / 2;
floor.position.y = 0.0;
scene.add(floor);

// TRANSFORMATIONS

function multMat(m1, m2){
  return new THREE.Matrix4().multiplyMatrices(m1, m2);
}

function inverseMat(m){
  return new THREE.Matrix4().getInverse(m, true);
}

function idMat4() {
  // Create Identity matrix
  // TODO

  return new THREE.Matrix4();
}

function translateMat(matrix, x, y, z) {
  // Apply translation [x, y, z] to @matrix
  // matrix: THREE.Matrix4
  // x, y, z: float

  // TODO

  //CHECK INVERSION
  const translationMatrix = idMat4();
  translationMatrix.set(
      1, 0, 0, x,
      0, 1, 0, y,
      0, 0, 1, z,
      0, 0, 0, 1
  );
  matrix = multMat(translationMatrix, matrix);
  return matrix;
}

function rotateMat(matrix, angle, axis){
  // Apply rotation by @angle with respect to @axis to @matrix
  // matrix: THREE.Matrix3
  // angle: float
  // axis: string "x", "y" or "z"

  // TODO

  const rotationMatrix = new THREE.Matrix4();
  switch (axis.toLowerCase()) {
    case 'x':
      rotationMatrix.set(
          1,0, 0, 0,
          0, Math.cos(angle), -(Math.sin(angle)), 0,
          0, Math.sin(angle), Math.cos(angle), 0,
          0, 0, 0, 1
      );
      break;
    case 'y':
      rotationMatrix.set(
          Math.cos(angle), 0, Math.sin(angle), 0,
          0, 1, 0, 0,
          -(Math.sin(angle)), 0, Math.cos(angle), 0,
          0, 0, 0, 1
      );
      break;
    case 'z':
      rotationMatrix.set(
          Math.cos(angle), -(Math.sin(angle)), 0, 0,
          Math.sin(angle), Math.cos(angle), 0, 0,
          0, 0, 1, 0,
          0, 0, 0, 1
      );
      break;
    default:
      break;
  }
  matrix = multMat(rotationMatrix, matrix);
  return matrix;
}

function rotateVec3(v, angle, axis){
  // Apply rotation by @angle with respect to @axis to vector @v
  // v: THREE.Vector3
  // angle: float
  // axis: string "x", "y" or "z"

  // TODO

  switch (axis.toLowerCase()) {
    case 'x':
      v.set(
          v.x,
          (v.y * Math.cos(angle))-(v.z * Math.sin(angle)),
          (v.y * Math.sin(angle))+(v.z * Math.cos(angle))
      );
      break;
    case 'y':
      v.set(
          (v.x * Math.cos(angle))+(v.z * Math.sin(angle)),
          v.y,
          (-(v.x) * Math.sin(angle))+(v.z * Math.cos(angle))
      );
      break;
    case 'z':
      v.set(
          (v.x * Math.cos(angle))-(v.y* Math.sin(angle)),
          (v.x * Math.cos(angle))-(v.y * Math.sin(angle)),
          v.z
      );
      break;
    default:
      break;
  }
  return v;
}

function rescaleMat(matrix, x, y, z){
  // Apply scaling @x, @y and @z to @matrix
  // matrix: THREE.Matrix3
  // x, y, z: float

  // TODO

  const rescaleMatrix = new THREE.Matrix4();
  rescaleMatrix.set(
      x, 0, 0, 0,
      0, y, 0, 0,
      0, 0, z, 0,
      0, 0, 0, 1
  );
  matrix = multMat(rescaleMatrix, matrix);
  return matrix;
}

class Robot {
  constructor() {
    // Geometry
    this.torsoHeight = 1.5;
    this.torsoRadius = 0.75;
    this.headRadius = 0.32;
    // Add parameters for parts
    // TODO

    this.armHeight = 0.25;
    this.armRadius = 0.32;

    this.leftForearmAngle = 0
    this.rightForearmAngle = 0
    this.leftLegAngle = 0
    this.rightLegAngle = 0

    // Animation
    this.walkDirection = new THREE.Vector3( 0, 0, 1 );

    this.ThighAngleAnim = 0; //Pour l'angle de la jambe
    this.halfWalk = true; //Pour si l'animation avance ou recule

    // Material
    this.material = new THREE.MeshNormalMaterial();

    // Initial pose
    this.initialize()
  }

  initialTorsoMatrix(){
    var initialTorsoMatrix = idMat4();
    initialTorsoMatrix = translateMat(initialTorsoMatrix, 0,this.torsoHeight/2 + 2, 0);
    return initialTorsoMatrix;
  }

  initialHeadMatrix(){
    var initialHeadMatrix = idMat4();
    initialHeadMatrix = translateMat(initialHeadMatrix, 0, this.torsoHeight/2 + this.headRadius, 0);

    return initialHeadMatrix;
  }

  initialArmMatrix(side, z){
    //side left, right
    var initialMatrix = idMat4();
    switch (side.toLowerCase()) {
      case 'l':
        initialMatrix = translateMat(initialMatrix,  (this.torsoRadius*4), this.torsoHeight, z);
        break;
      case 'r':
        initialMatrix = translateMat(initialMatrix,  -(this.torsoRadius*4), this.torsoHeight, z);
        break;
      default:
        break;
    }
    initialMatrix= rescaleMat(initialMatrix, this.armHeight, this.armHeight, 1);
    return initialMatrix;
  }
  initialThighMatrix(side, y){
    //side left, right
    var initialMatrix = idMat4();
    switch (side.toLowerCase()) {
      case 'l':
        initialMatrix = translateMat(initialMatrix,  (this.torsoRadius*2), -((this.torsoHeight*0.75)+y), 0);
        break;
      case 'r':
        initialMatrix = translateMat(initialMatrix,  -(this.torsoRadius*2), -((this.torsoHeight*0.75)+y), 0);
        break;
      default:
        break;
    }
    initialMatrix= rescaleMat(initialMatrix, this.armHeight, 1, this.armHeight);
    return initialMatrix;
  }

  initialize() {
    // Torso
    var torsoGeometry = new THREE.CubeGeometry(2*this.torsoRadius, this.torsoHeight, this.torsoRadius, 64);
    this.torso = new THREE.Mesh(torsoGeometry, this.material);

    // Head
    var headGeometry = new THREE.CubeGeometry(2*this.headRadius, this.headRadius, this.headRadius);
    this.head = new THREE.Mesh(headGeometry, this.material);

    // Add parts
    // TODO

    var armGeometry = new THREE.SphereGeometry(2*this.armRadius, 32, 16);
    this.leftArm = new THREE.Mesh(armGeometry, this.material);
    this.rightArm = new THREE.Mesh(armGeometry, this.material);
    this.leftForearm = new THREE.Mesh(armGeometry, this.material);
    this.rightForearm = new THREE.Mesh(armGeometry, this.material);
    this.leftThigh = new THREE.Mesh(armGeometry, this.material);
    this.rightThigh = new THREE.Mesh(armGeometry, this.material);
    this.leftLeg = new THREE.Mesh(armGeometry, this.material);
    this.rightLeg = new THREE.Mesh(armGeometry, this.material);

    // Torse transformation
    this.torsoInitialMatrix = this.initialTorsoMatrix();
    this.torsoMatrix = idMat4();
    this.torso.setMatrix(this.torsoInitialMatrix);

    // Head transformation
    this.headInitialMatrix = this.initialHeadMatrix();
    this.headMatrix = idMat4();
    var matrix = multMat(this.torsoInitialMatrix, this.headInitialMatrix);
    this.head.setMatrix(matrix);

    // Add transformations
    // TODO

    //arm transformation
    this.leftArmInitialMatrix = this.initialArmMatrix('l', 0.5);
    this.leftArmMatrix = idMat4();
    var matrixLeftArm = multMat(this.torsoInitialMatrix, this.leftArmInitialMatrix);
    this.leftArm.setMatrix(matrixLeftArm);

    this.rightArmInitialMatrix = this.initialArmMatrix('r', 0.5);
    this.rightArmMatrix = idMat4();
    var matrixRightArm = multMat(this.torsoInitialMatrix, this.rightArmInitialMatrix);
    this.rightArm.setMatrix(matrixRightArm);

    this.leftForearmInitialMatrix = this.initialArmMatrix('l', 1.5);
    this.leftForearmMatrix = idMat4();
    var matrixLeftForearm = multMat(this.torsoInitialMatrix, this.leftForearmInitialMatrix);
    this.leftForearm.setMatrix(matrixLeftForearm);

    this.rightForearmInitialMatrix = this.initialArmMatrix('r', 1.5);
    this.rightForearmMatrix = idMat4();
    var matrixRightForearm = multMat(this.torsoInitialMatrix, this.rightForearmInitialMatrix);
    this.rightForearm.setMatrix(matrixRightForearm);

    this.leftThighInitialMatrix = this.initialThighMatrix('l', 0);
    this.leftThighMatrix = idMat4();
    var matrixLeftThigh = multMat(this.torsoInitialMatrix, this.leftThighInitialMatrix);
    this.leftThigh.setMatrix(matrixLeftThigh);

    this.rightThighInitialMatrix = this.initialThighMatrix('r', 0);
    this.rightThighMatrix = idMat4();
    var matrixRightThigh = multMat(this.torsoInitialMatrix, this.rightThighInitialMatrix);
    this.rightThigh.setMatrix(matrixRightThigh);

    this.leftLegInitialMatrix = this.initialThighMatrix('l', 1);
    this.leftLegMatrix = idMat4();
    var matrixLeftLeg = multMat(this.torsoInitialMatrix, this.leftLegInitialMatrix);
    this.leftLeg.setMatrix(matrixLeftLeg);

    this.rightLegInitialMatrix = this.initialThighMatrix('r', 1);
    this.rightLegMatrix = idMat4();
    var matrixRightLeg = multMat(this.torsoInitialMatrix, this.rightLegInitialMatrix);
    this.rightLeg.setMatrix(matrixRightLeg);

	// Add robot to scene
	scene.add(this.torso);
    scene.add(this.head);
    // Add parts
    // TODO

    scene.add(this.leftArm);
    scene.add(this.rightArm);
    scene.add(this.leftForearm);
    scene.add(this.rightForearm);
    scene.add(this.leftThigh);
    scene.add(this.rightThigh);
    scene.add(this.leftLeg);
    scene.add(this.rightLeg);
  }

  rotateTorso(angle){
    var torsoMatrix = this.torsoMatrix;

    this.torsoMatrix = idMat4();
    this.torsoMatrix = rotateMat(this.torsoMatrix, angle, "y");
    this.torsoMatrix = multMat(torsoMatrix, this.torsoMatrix);

    var matrix = multMat(this.torsoMatrix, this.torsoInitialMatrix);
    this.torso.setMatrix(matrix);

    //check

    var matrixhead = multMat(this.headMatrix, this.headInitialMatrix);
    matrixhead = multMat(matrix, matrixhead);
    this.head.setMatrix(matrixhead);

    var matrixLeftArm = multMat(this.leftArmMatrix, this.leftArmInitialMatrix);
    matrixLeftArm = multMat(matrix, matrixLeftArm);
    this.leftArm.setMatrix(matrixLeftArm);
    var matrixLeftForearm = multMat(this.leftForearmMatrix, this.leftForearmInitialMatrix);
    matrixLeftForearm = multMat(matrix, matrixLeftForearm);
    this.leftForearm.setMatrix(matrixLeftForearm);

    var matrixRightArm = multMat(this.rightArmMatrix, this.rightArmInitialMatrix);
    matrixRightArm = multMat(matrix, matrixRightArm);
    this.rightArm.setMatrix(matrixRightArm);
    var matrixRightForearm = multMat(this.rightForearmMatrix, this.rightForearmInitialMatrix);
    matrixRightForearm = multMat(matrix, matrixRightForearm);
    this.rightForearm.setMatrix(matrixRightForearm);

    var matrixLeftThigh = multMat(this.leftThighMatrix, this.leftThighInitialMatrix);
    matrixLeftThigh = multMat(matrix, matrixLeftThigh);
    this.leftThigh.setMatrix(matrixLeftThigh);
    var matrixLeftLeg = multMat(this.leftLegMatrix, this.leftLegInitialMatrix);
    matrixLeftLeg = multMat(matrix, matrixLeftLeg);
    this.leftLeg.setMatrix(matrixLeftLeg);

    var matrixRightThigh = multMat(this.rightThighMatrix, this.rightThighInitialMatrix);
    matrixRightThigh = multMat(matrix, matrixRightThigh);
    this.rightThigh.setMatrix(matrixRightThigh);
    var matrixRightLeg = multMat(this.rightLegMatrix, this.rightLegInitialMatrix);
    matrixRightLeg = multMat(matrix, matrixRightLeg);
    this.rightLeg.setMatrix(matrixRightLeg);

    this.walkDirection = rotateVec3(this.walkDirection, angle, "y");
  }

  moveTorso(speed){
    this.torsoMatrix = translateMat(this.torsoMatrix, speed * this.walkDirection.x, speed * this.walkDirection.y, speed * this.walkDirection.z);

    var matrix = multMat(this.torsoMatrix, this.torsoInitialMatrix);
    this.torso.setMatrix(matrix);

    //check
    var matrixhead = multMat(this.headMatrix, this.headInitialMatrix);
    matrixhead = multMat(matrix, matrixhead);
    this.head.setMatrix(matrixhead);

    var matrixLeftArm = multMat(this.leftArmMatrix, this.leftArmInitialMatrix);
    matrixLeftArm = multMat(matrix, matrixLeftArm);
    this.leftArm.setMatrix(matrixLeftArm);
    var matrixLeftForearm = multMat(this.leftForearmMatrix, this.leftForearmInitialMatrix);
    matrixLeftForearm = multMat(matrix, matrixLeftForearm);
    this.leftForearm.setMatrix(matrixLeftForearm);

    var matrixRightArm = multMat(this.rightArmMatrix, this.rightArmInitialMatrix);
    matrixRightArm = multMat(matrix, matrixRightArm);
    this.rightArm.setMatrix(matrixRightArm);
    var matrixRightForearm = multMat(this.rightForearmMatrix, this.rightForearmInitialMatrix);
    matrixRightForearm = multMat(matrix, matrixRightForearm);
    this.rightForearm.setMatrix(matrixRightForearm);

    var matrixLeftThigh = multMat(this.leftThighMatrix, this.leftThighInitialMatrix);
    matrixLeftThigh = multMat(matrix, matrixLeftThigh);
    this.leftThigh.setMatrix(matrixLeftThigh);
    var matrixLeftLeg = multMat(this.leftLegMatrix, this.leftLegInitialMatrix);
    matrixLeftLeg = multMat(matrix, matrixLeftLeg);
    this.leftLeg.setMatrix(matrixLeftLeg);

    var matrixRightThigh = multMat(this.rightThighMatrix, this.rightThighInitialMatrix);
    matrixRightThigh = multMat(matrix, matrixRightThigh);
    this.rightThigh.setMatrix(matrixRightThigh);
    var matrixRightLeg = multMat(this.rightLegMatrix, this.rightLegInitialMatrix);
    matrixRightLeg = multMat(matrix, matrixRightLeg);
    this.rightLeg.setMatrix(matrixRightLeg);
  }

  rotateHead(angle){
    var headMatrix = this.headMatrix;

    this.headMatrix = idMat4();
    this.headMatrix = rotateMat(this.headMatrix, angle, "y");
    this.headMatrix = multMat(headMatrix, this.headMatrix);

    var matrix = multMat(this.headMatrix, this.headInitialMatrix);
    matrix = multMat(this.torsoMatrix, matrix);
    matrix = multMat(this.torsoInitialMatrix, matrix);
    this.head.setMatrix(matrix);
  }

  // Add methods for other parts
  // TODO

  rotateArm(angle, side, axis){
    // side left, right
    var armPivotPoint = new THREE.Vector3(-0.75, -0.375, 0);
    switch (side.toLowerCase()) {
      case 'l':
        var leftArmMatrix = this.leftArmMatrix;

        this.leftArmMatrix = idMat4();
        this.leftArmMatrix = translateMat(this.leftArmMatrix, armPivotPoint.x, armPivotPoint.y, armPivotPoint.z);
        this.leftArmMatrix = rotateMat(this.leftArmMatrix, angle, axis);
        this.leftArmMatrix = translateMat(this.leftArmMatrix, -armPivotPoint.x, -armPivotPoint.y, -armPivotPoint.z);
        this.leftArmMatrix = multMat(leftArmMatrix, this.leftArmMatrix);

        var matrix = multMat(this.leftArmMatrix, this.leftArmInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.leftArm.setMatrix(matrix);

        this.rotateForearm(-this.leftForearmAngle, "l", false);

        var leftForearmMatrix = this.leftForearmMatrix;

        this.leftForearmMatrix = idMat4();
        this.leftForearmMatrix = translateMat(this.leftForearmMatrix, armPivotPoint.x, armPivotPoint.y, armPivotPoint.z);
        this.leftForearmMatrix = rotateMat(this.leftForearmMatrix, angle, axis);
        this.leftForearmMatrix = translateMat(this.leftForearmMatrix, -armPivotPoint.x, -armPivotPoint.y, -armPivotPoint.z);
        this.leftForearmMatrix = multMat(leftForearmMatrix, this.leftForearmMatrix);

        this.rotateForearm(this.leftForearmAngle, "l", false);

        matrix = multMat(this.leftForearmMatrix, this.leftForearmInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.leftForearm.setMatrix(matrix);

        break;
      case 'r':
        var rightArmMatrix = this.rightArmMatrix;

        this.rightArmMatrix = idMat4();
        this.rightArmMatrix = translateMat(this.rightArmMatrix, -armPivotPoint.x, armPivotPoint.y, armPivotPoint.z);
        this.rightArmMatrix = rotateMat(this.rightArmMatrix, angle, axis);
        this.rightArmMatrix = translateMat(this.rightArmMatrix, armPivotPoint.x, -armPivotPoint.y, -armPivotPoint.z);
        this.rightArmMatrix = multMat(rightArmMatrix, this.rightArmMatrix);


        var matrix = multMat(rightArmMatrix, this.rightArmInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.rightArm.setMatrix(matrix);

        this.rotateForearm(-this.rightForearmAngle, "r", false);

        var rightForearmMatrix = this.rightForearmMatrix;

        this.rightForearmMatrix = idMat4();
        this.rightForearmMatrix = translateMat(this.rightForearmMatrix, -armPivotPoint.x, armPivotPoint.y, armPivotPoint.z);
        this.rightForearmMatrix = rotateMat(this.rightForearmMatrix, angle, axis);
        this.rightForearmMatrix = translateMat(this.rightForearmMatrix, armPivotPoint.x, -armPivotPoint.y, -armPivotPoint.z);
        this.rightForearmMatrix = multMat(rightForearmMatrix, this.rightForearmMatrix);

        this.rotateForearm(this.rightForearmAngle, "r", false);

        matrix = multMat(this.rightForearmMatrix, this.rightForearmInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.rightForearm.setMatrix(matrix);
      default:
        break;
    }
  }

  rotateForearm(angle, side, bool){
    // side left, right
    var forearmPivotPoint = new THREE.Vector3(0, -0.375, -1);
    switch (side.toLowerCase()) {
      case 'l':
        var leftForearmMatrix = this.leftForearmMatrix;

        this.leftForearmMatrix = idMat4();
        this.leftForearmMatrix = translateMat(this.leftForearmMatrix, forearmPivotPoint.x, forearmPivotPoint.y, forearmPivotPoint.z);
        this.leftForearmMatrix = rotateMat(this.leftForearmMatrix, angle, "x");
        this.leftForearmMatrix = translateMat(this.leftForearmMatrix, -forearmPivotPoint.x, -forearmPivotPoint.y, -forearmPivotPoint.z);
        this.leftForearmMatrix = multMat(leftForearmMatrix, this.leftForearmMatrix);

        var matrix = multMat(this.leftForearmMatrix, this.leftForearmInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.leftForearm.setMatrix(matrix);
        if (bool){
          this.leftForearmAngle += angle;
        }
        break;
      case 'r':
        var rightForearmMatrix = this.rightForearmMatrix;

        this.rightForearmMatrix = idMat4();
        this.rightForearmMatrix = translateMat(this.rightForearmMatrix, forearmPivotPoint.x, forearmPivotPoint.y, forearmPivotPoint.z);
        this.rightForearmMatrix = rotateMat(this.rightForearmMatrix, angle, "x");
        this.rightForearmMatrix = translateMat(this.rightForearmMatrix, -forearmPivotPoint.x, -forearmPivotPoint.y, -forearmPivotPoint.z);
        this.rightForearmMatrix = multMat(rightForearmMatrix, this.rightForearmMatrix);

        var matrix = multMat(this.rightForearmMatrix, this.rightForearmInitialMatrix);
        matrix = multMat(this.rightArmMatrix, matrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.rightForearm.setMatrix(matrix);
        if (bool){
          this.rightForearmAngle += angle;
        }
      default:
        break;
    }
  }

  rotateThigh(angle, side){
    // side left, right
    var thighPivotPoint = new THREE.Vector3(0, 0.75, 0);
    //var thighPivotPoint = new THREE.Vector3(-0.75, -0.375, 0);
    switch (side.toLowerCase()) {
      case 'l':
        var leftThighMatrix = this.leftThighMatrix;

        this.leftThighMatrix = idMat4();
        this.leftThighMatrix = translateMat(this.leftThighMatrix, thighPivotPoint.x, thighPivotPoint.y, thighPivotPoint.z);
        this.leftThighMatrix = rotateMat(this.leftThighMatrix, angle, "x");
        this.leftThighMatrix = translateMat(this.leftThighMatrix, -thighPivotPoint.x, -thighPivotPoint.y, -thighPivotPoint.z);
        this.leftThighMatrix = multMat(leftThighMatrix, this.leftThighMatrix);

        var matrix = multMat(this.leftThighMatrix, this.leftThighInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.leftThigh.setMatrix(matrix);

        this.rotateLeg(-this.leftLegAngle, "l", false);

        var leftLegMatrix = this.leftLegMatrix;

        this.leftLegMatrix = idMat4();
        this.leftLegMatrix = translateMat(this.leftLegMatrix, -thighPivotPoint.x, thighPivotPoint.y, thighPivotPoint.z);
        this.leftLegMatrix = rotateMat(this.leftLegMatrix, angle, "x");
        this.leftLegMatrix = translateMat(this.leftLegMatrix, thighPivotPoint.x, -thighPivotPoint.y, -thighPivotPoint.z);
        this.leftLegMatrix = multMat(leftLegMatrix, this.leftLegMatrix);

        this.rotateLeg(this.leftLegAngle, "l", false);

        matrix = multMat(this.leftLegMatrix, this.leftLegInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.leftLeg.setMatrix(matrix);
        break;
      case 'r':
        var rightThighMatrix = this.rightThighMatrix;

        this.rightThighMatrix = idMat4();
        this.rightThighMatrix = translateMat(this.rightThighMatrix, thighPivotPoint.x, thighPivotPoint.y, thighPivotPoint.z);
        this.rightThighMatrix = rotateMat(this.rightThighMatrix, angle, "x");
        this.rightThighMatrix = translateMat(this.rightThighMatrix, -thighPivotPoint.x, -thighPivotPoint.y, -thighPivotPoint.z);
        this.rightThighMatrix = multMat(rightThighMatrix, this.rightThighMatrix);

        var matrix = multMat(this.rightThighMatrix, this.rightThighInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.rightThigh.setMatrix(matrix);

        this.rotateLeg(-this.rightLegAngle, "r", false);

        var rightLegMatrix = this.rightLegMatrix;

        this.rightLegMatrix = idMat4();
        this.rightLegMatrix = translateMat(this.rightLegMatrix, -thighPivotPoint.x, thighPivotPoint.y, thighPivotPoint.z);
        this.rightLegMatrix = rotateMat(this.rightLegMatrix, angle, "x");
        this.rightLegMatrix = translateMat(this.rightLegMatrix, thighPivotPoint.x, -thighPivotPoint.y, -thighPivotPoint.z);
        this.rightLegMatrix = multMat(rightLegMatrix, this.rightLegMatrix);

        this.rotateLeg(this.rightLegAngle, "r", false);

        matrix = multMat(this.rightLegMatrix, this.rightLegInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.rightLeg.setMatrix(matrix);
      default:
        break;
    }
  }

  rotateLeg(angle, side, bool){
    // side left, right
    var legPivotPoint = new THREE.Vector3(0, 1.75, 0);
    //var legPivotPoint = new THREE.Vector3(-0.75, -0.375, 0);
    switch (side.toLowerCase()) {
      case 'l':
        var leftLegMatrix = this.leftLegMatrix;

        this.leftLegMatrix = idMat4();
        this.leftLegMatrix = translateMat(this.leftLegMatrix, legPivotPoint.x, legPivotPoint.y, legPivotPoint.z);
        this.leftLegMatrix = rotateMat(this.leftLegMatrix, angle, "x");
        this.leftLegMatrix = translateMat(this.leftLegMatrix, -legPivotPoint.x, -legPivotPoint.y, -legPivotPoint.z);
        this.leftLegMatrix = multMat(leftLegMatrix, this.leftLegMatrix);

        var matrix = multMat(this.leftLegMatrix, this.leftLegInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.leftLeg.setMatrix(matrix);
        if (bool){
          this.leftLegAngle += angle;
        }
        break;
      case 'r':
        var rightLegMatrix = this.rightLegMatrix;

        this.rightLegMatrix = idMat4();
        this.rightLegMatrix = translateMat(this.rightLegMatrix, legPivotPoint.x, legPivotPoint.y, legPivotPoint.z);
        this.rightLegMatrix = rotateMat(this.rightLegMatrix, angle, "x");
        this.rightLegMatrix = translateMat(this.rightLegMatrix, -legPivotPoint.x, -legPivotPoint.y, -legPivotPoint.z);
        this.rightLegMatrix = multMat(rightLegMatrix, this.rightLegMatrix);

        var matrix = multMat(this.rightLegMatrix, this.rightLegInitialMatrix);
        matrix = multMat(this.torsoMatrix, matrix);
        matrix = multMat(this.torsoInitialMatrix, matrix);
        this.rightLeg.setMatrix(matrix);
        if (bool){
          this.rightLegAngle += angle;
        }
      default:
        break;
    }
  }

  animationWalk(speed){

    if(this.ThighAngleAnim <= -0.5){
      this.halfWalk = true;
    } else if(this.ThighAngleAnim >= 0.5) {
      this.halfWalk = false;
    }

    if (this.halfWalk) {
      this.rotateThigh(speed, "r");
      this.rotateThigh(-speed, "l");
      this.ThighAngleAnim += speed;
    } else {
      this.rotateThigh(-speed, "r");
      this.rotateThigh(speed, "l");
      this.ThighAngleAnim -= speed;
    }
    this.walkUpdate();
  }
  walkUpdate(){
    var leftLegY = this.leftLeg.position.y;
    var rightLegY = this.rightLeg.position.y;

    var originalWalkDirection = this.walkDirection;

    var deltaY = -(Math.min(leftLegY, rightLegY)) + 2*this.armRadius*Math.cos(this.ThighAngleAnim);

    this.walkDirection = new THREE.Vector3(0, deltaY, 0);
    this.moveTorso(1);

    this.walkDirection = originalWalkDirection;
  }
}

var robot = new Robot();

// LISTEN TO KEYBOARD
var keyboard = new THREEx.KeyboardState();

var selectedRobotComponent = 0;
var components = [
  "Torso",
  "Head",
  // Add parts names
  // TODO
  "leftArm",
  "rightArm",
  "leftForearm",
  "rightForearm",
  "leftThigh",
  "rightThigh",
  "leftLeg",
  "rightLeg"

];
var numberComponents = components.length;

function checkKeyboard() {
  // Next element
  if (keyboard.pressed("e")){
    selectedRobotComponent = selectedRobotComponent + 1;

    if (selectedRobotComponent<0){
      selectedRobotComponent = numberComponents - 1;
    }

    if (selectedRobotComponent >= numberComponents){
      selectedRobotComponent = 0;
    }

    window.alert(components[selectedRobotComponent] + " selected");
  }

  // Previous element
  if (keyboard.pressed("q")){
    selectedRobotComponent = selectedRobotComponent - 1;

    if (selectedRobotComponent < 0){
      selectedRobotComponent = numberComponents - 1;
    }

    if (selectedRobotComponent >= numberComponents){
      selectedRobotComponent = 0;
    }

    window.alert(components[selectedRobotComponent] + " selected");
  }

  // UP
  if (keyboard.pressed("w")){
    switch (components[selectedRobotComponent]){
      case "Torso":
        robot.moveTorso(0.1);
        robot.animationWalk(0.05)
        break;
      case "Head":
        break;
      // Add more cases
      // TODO

      case "leftArm":
        robot.rotateArm(-0.05, "l", "x");
        break;
      case "rightArm":
        robot.rotateArm(-0.05, "r", "x");
        break;
      case "leftForearm":
        robot.rotateForearm(-0.05, "l", true);
        break;
      case "rightForearm":
        robot.rotateForearm(-0.05, "r", true);
        break;
      case "leftThigh":
        robot.rotateThigh(-0.05, "l");
        break;
      case "rightThigh":
        robot.rotateThigh(-0.05, "r");
        break;
      case "leftLeg":
        robot.rotateLeg(-0.05, "l", true);
        break;
      case "rightLeg":
        robot.rotateLeg(-0.05, "r", true);
        break;
      default:
        break;
    }
  }

  // DOWN
  if (keyboard.pressed("s")){
    switch (components[selectedRobotComponent]){
      case "Torso":
        robot.moveTorso(-0.1);
        robot.animationWalk(0.05)
        break;
      case "Head":
        break;
      // Add more cases
      // TODO

      case "leftArm":
        robot.rotateArm(0.05, "l", "x");
        break;
      case "rightArm":
        robot.rotateArm(0.05, "r", "x");
        break;
      case "leftForearm":
        robot.rotateForearm(0.05, "l", true);
        break;
      case "rightForearm":
        robot.rotateForearm(0.05, "r", true);
        break;
      case "leftThigh":
        robot.rotateThigh(0.05, "l");
        break;
      case "rightThigh":
        robot.rotateThigh(0.05, "r");
        break;
      case "leftLeg":
        robot.rotateLeg(0.05, "l", true);
        break;
      case "rightLeg":
        robot.rotateLeg(0.05, "r", true);
        break;
      default:
        break;
    }
  }

  // LEFT
  if (keyboard.pressed("a")){
    switch (components[selectedRobotComponent]){
      case "Torso":
        robot.rotateTorso(0.1);
        break;
      case "Head":
        robot.rotateHead(0.1);
        break;
      // Add more cases
      // TODO

      case "leftArm":
        robot.rotateArm(0.05, "l", "y");
        break;
      case "rightArm":
        robot.rotateArm(0.05, "r", "y");
        break;
      default:
        break;
    }
  }

  // RIGHT
  if (keyboard.pressed("d")){
    switch (components[selectedRobotComponent]){
      case "Torso":
        robot.rotateTorso(-0.1);
        break;
      case "Head":
        robot.rotateHead(-0.1);
        break;
      // Add more cases
      // TODO

      case "leftArm":
        robot.rotateArm(-0.05, "l", "y");
        break;
      case "rightArm":
        robot.rotateArm(-0.05, "r", "y");
        break;
      default:
        break;
    }
  }
}

// SETUP UPDATE CALL-BACK
function update() {
  checkKeyboard();
  requestAnimationFrame(update);
  renderer.render(scene, camera);
}

update();
