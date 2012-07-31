/*
Copyright (c) 2012 Paul Richards <paul.richards@gmail.com>

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

var supersample = 2.0 / 1.0;

function asyncGetShader(gl, url, type, callback) {
    $.get(url, function(data) {
        var shader = gl.createShader(type);
        gl.shaderSource(shader, data);
        gl.compileShader(shader);

        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
            alert(gl.getShaderInfoLog(shader));
        } else {
            callback(shader);
        }
    }).error(function(e) {
        alert("error: " + e);
    });
}

function lookupAxis(letter) {
    switch (letter) {
    case 'A':
        return [ 2.0, 0.0, 0.0, 0.0 ];
    case 'B':
        return [ 0.0, 2.0, 0.0, 0.0 ];
    case 'C':
        return [ 0.0, 0.0, 2.0, 0.0 ];
    case 'D':
        return [ 0.0, 0.0, 0.0, 2.0 ];
    default:
        alert("Unrecognised axis: " + letter);
    }
}

function initGL(canvas) {
    var gl;
    try {
        gl = canvas.getContext("experimental-webgl");
    } catch (e) {
        alert("error: " + e);
    }
    if (!gl) {
        alert("Failed to initialise WebGL");
    }
    return gl;
}

// center + x * axisX + y * axisY
function mult(center, x, axisX, y, axisY) {
    return [ //
    center[0] + x * axisX[0] + y * axisY[0], //
    center[1] + x * axisX[1] + y * axisY[1], //
    center[2] + x * axisX[2] + y * axisY[2], //
    center[3] + x * axisX[3] + y * axisY[3] ];
}

function attachToCanvas(canvas, axisX, axisY, center) {
    var gl;
    var aVertexPosition;
    var aVolumePosition;

    function webGLStart() {
        gl = initGL(canvas);
        var vertexShader = null;
        var fragmentShader = null;

        var areWeDone = function() {
            if (vertexShader != null && fragmentShader != null) {
                var shaderProgram = gl.createProgram();
                gl.attachShader(shaderProgram, vertexShader);
                gl.attachShader(shaderProgram, fragmentShader);
                gl.linkProgram(shaderProgram);

                if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
                    alert("Could not initialise shaders");
                }

                gl.useProgram(shaderProgram);

                aVertexPosition = gl.getAttribLocation(shaderProgram,
                        "aVertexPosition");
                gl.enableVertexAttribArray(aVertexPosition);

                aVolumePosition = gl.getAttribLocation(shaderProgram,
                        "aVolumePosition");
                gl.enableVertexAttribArray(aVolumePosition);

                gl.clearColor(0.0, 0.0, 0.0, 1.0);
                gl.clearDepth(1.0);

                center.register(redraw);
                redraw();
            }
        };

        asyncGetShader(gl, "fragment.glsl", gl.FRAGMENT_SHADER, //
        function(shader) {
            fragmentShader = shader;
            areWeDone();
        });
        asyncGetShader(gl, "vertex.glsl", gl.VERTEX_SHADER, //
        function(shader) {
            vertexShader = shader;
            areWeDone();
        });
    }

    function redraw() {
        canvas.width = Math.round(canvas.clientWidth * supersample);
        canvas.height = Math.round(canvas.clientHeight * supersample);
        gl.viewport(0, 0, canvas.width, canvas.height);

        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
        var scaleX = Math.max(1.0, canvas.width / canvas.height);
        var scaleY = Math.max(1.0, canvas.height / canvas.width);

        // Vertex positions in screen space
        var vertexPositionBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, vertexPositionBuffer);
        var vertices = [ 1.0, -1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0 ];
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices),
                gl.STATIC_DRAW);
        gl.vertexAttribPointer(aVertexPosition, 2, gl.FLOAT, false, 0, 0);

        // Vertex positions in volume space
        var volumePositionBuffer = gl.createBuffer();
        gl.bindBuffer(gl.ARRAY_BUFFER, volumePositionBuffer);
        var vertices = [].concat( //
        mult(center.get(), scaleX, axisX, -scaleY, axisY), //
        mult(center.get(), -scaleX, axisX, -scaleY, axisY), //
        mult(center.get(), scaleX, axisX, scaleY, axisY), //
        mult(center.get(), -scaleX, axisX, scaleY, axisY));
        gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices),
                gl.STATIC_DRAW);
        gl.vertexAttribPointer(aVolumePosition, 4, gl.FLOAT, false, 0, 0);

        gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);

        gl.deleteBuffer(vertexPositionBuffer);
        gl.deleteBuffer(volumePositionBuffer);
    }

    webGLStart();

    var oldPos = null;
    $(canvas).mousedown(function(e) {
        oldPos = [ e.pageX, e.pageY ];
    });

    $(canvas)
            .mousemove(
                    function(e) {
                        if (oldPos) {
                            var newPos = [ e.pageX, e.pageY ];
                            var delta = [ newPos[0] - oldPos[0],
                                    newPos[1] - oldPos[1] ];
                            oldPos = newPos;

                            var scaleX = -2.0 / canvas.offsetWidth;
                            var scaleY = 2.0 / canvas.offsetHeight;
                            var oldC = center.get();
                            var newC = mult(oldC, delta[0] * scaleX, axisX,
                                    delta[1] * scaleY, axisY);
                            center.set(newC);
                        }
                    });

    $(canvas).mouseup(function(e) {
        oldPos = null;
    });
}

$(document).ready(
        function() {

            var redrawFns = [];
            function redrawAll() {
                $(redrawFns).each(function(idx, fn) {
                    fn();
                });
            }

            var center = {
                get : function() {
                    var a = window.location.hash.match(/a=([^\/]*)/);
                    var b = window.location.hash.match(/b=([^\/]*)/);
                    var c = window.location.hash.match(/c=([^\/]*)/);
                    var d = window.location.hash.match(/d=([^\/]*)/);

                    return [ //
                    a ? parseFloat(a[1]) : 0.0, //
                    b ? parseFloat(b[1]) : 0.0, //
                    c ? parseFloat(c[1]) : 0.0, //
                    d ? parseFloat(d[1]) : 0.0 ];
                },
                set : function(newValue) {
                    var newHash = //
                    "a=" + newValue[0] + "/" + "b=" + newValue[1] + "/" + "c="
                            + newValue[2] + "/" + "d=" + newValue[3];
                    window.location.hash = newHash;
                    // redraw will be triggered by the hash changing..
                },
                register : function(fn) {
                    redrawFns.push(fn);
                }
            };

            $("canvas").each(function(idx, elem) {
                var id = $(elem).attr('id');
                var axisX = lookupAxis(id[id.length - 2]);
                var axisY = lookupAxis(id[id.length - 1]);
                attachToCanvas(elem, axisX, axisY, center);
            });

            $(window).resize(redrawAll);

            $(window).bind('hashchange', function() {
                redrawAll();
            });
        });
