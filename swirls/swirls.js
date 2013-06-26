$(document)
        .ready(
                function() {
                    var WIDTH = 720;
                    var HEIGHT = 720;

                    var $container = $('#container');

                    var renderer = new THREE.WebGLRenderer();
                    var camera = new THREE.OrthographicCamera(-1.0, 1.0, -1.0,
                            1.0, -1.0, 1.0);
                    camera.position.x = 0.0;
                    camera.position.y = 0.0;
                    camera.position.z = 0.0;

                    renderer.setSize(WIDTH, HEIGHT);
                    $container.append(renderer.domElement);

                    var geometry = new THREE.Geometry();
                    geometry.vertices.push(new THREE.Vector3(-1, -1, 0.0));
                    geometry.vertices.push(new THREE.Vector3(-1, 1, 0.0));
                    geometry.vertices.push(new THREE.Vector3(1, 1, 0.0));
                    geometry.vertices.push(new THREE.Vector3(1, -1, 0.0));
                    geometry.faces.push(new THREE.Face3(0, 1, 2));
                    geometry.faces.push(new THREE.Face3(0, 2, 3));

                    var spotCount = 25;

                    var uniforms = {
                        colorMode : {
                            type : 'i',
                            value : 0
                        },
                        spotCount : {
                            type : 'i',
                            value : spotCount
                        },
                        spotPositions : {
                            type : 'v2v',
                            value : [],
                        },
                        spotMass : {
                            type : 'f',
                            value : 1000.0
                        }
                    };

                    var shaderMaterial = new THREE.ShaderMaterial({
                        uniforms : uniforms,
                        vertexShader : $('#vertexshader').text(),
                        fragmentShader : $('#fragmentshader').text()
                    });

                    var mesh = new THREE.Mesh(geometry, shaderMaterial);

                    var scene = new THREE.Scene();
                    scene.add(mesh);

                    var baseTime = new Date().getTime();
                    var speed = 0.1;
                    var controlPoints = [];
                    for ( var i = 0; i < spotCount; i++) {
                        controlPoints[i] = {
                            x : [ Math.random(), Math.random(), Math.random(),
                                    Math.random() ],
                            y : [ Math.random(), Math.random(), Math.random(),
                                    Math.random() ]
                        };
                    }

                    var interp = function(p, u) {
                        var a = -u * u * u + 2 * u * u - u;
                        var b = 3 * u * u * u - 5 * u * u + 2;
                        var c = -3 * u * u * u + 4 * u * u + u;
                        var d = u * u * u - u * u;
                        return 0.5 * (a * p[0] + b * p[1] + c * p[2] + d * p[3]);
                    };

                    var oldTime = 0;
                    var update = function() {
                        var time = ((new Date().getTime() - baseTime) / 1000.0)
                                * speed;
                        if (Math.floor(time) != Math.floor(oldTime)) {
                            for ( var i = 0; i < spotCount; i++) {
                                controlPoints[i].x.shift();
                                controlPoints[i].x.push(Math.random());
                                controlPoints[i].y.shift();
                                controlPoints[i].y.push(Math.random());
                            }
                        }
                        oldTime = time;

                        var u = time % 1.0;
                        for ( var i = 0; i < spotCount; i++) {
                            C = controlPoints[i];
                            var x = interp(C.x, u);
                            var y = interp(C.y, u);
                            uniforms.spotPositions.value[i] = new THREE.Vector2(
                                    x * WIDTH, y * HEIGHT);
                        }

                        uniforms.colorMode.value = $(
                                'input:radio[name=mode]:checked').val();

                        renderer.render(scene, camera);

                        // set up the next call
                        requestAnimationFrame(update);
                    };

                    requestAnimationFrame(update);
                });
