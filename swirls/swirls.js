$(document).ready(
        function() {
            var WIDTH = 720;
            var HEIGHT = 720;

            var $container = $('#container');

            var renderer = new THREE.WebGLRenderer();
            var camera = new THREE.OrthographicCamera(-1.0, 1.0, -1.0, 1.0,
                    -1.0, 1.0);
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
            var speed = 0.01;
            var somePrimes = [ 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
                    43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103,
                    107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167,
                    173 ];

            var update = function() {
                var t = 20 + ((new Date().getTime() - baseTime) / 1000.0)
                        * speed;

                uniforms.colorMode.value = $('input:radio[name=mode]:checked')
                        .val();
                for ( var i = 0; i < spotCount; i++) {
                    uniforms.spotPositions.value[i] = new THREE.Vector2(Math
                            .cos(t * somePrimes[i])
                            * (WIDTH / 3) + (WIDTH / 2), Math.cos(t
                            * somePrimes[i + 1])
                            * (HEIGHT / 3) + (HEIGHT / 2));
                }
                renderer.render(scene, camera);

                // set up the next call
                requestAnimationFrame(update);
            };

            requestAnimationFrame(update);
        });
