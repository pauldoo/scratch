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
    var maximized = false;

    function webGLStart() {
        gl = WebGLUtils.setupWebGL(canvas);
        if (gl == null) {
            return;
        }
        var vertexShader = null;
        var fragmentShader = null;

        var areWeDone = function() {
            if (vertexShader != null && fragmentShader != null) {
                var shaderProgram = gl.createProgram();
                gl.attachShader(shaderProgram, vertexShader);
                gl.attachShader(shaderProgram, fragmentShader);
                gl.linkProgram(shaderProgram);

                var linked = gl.getProgramParameter(shaderProgram, gl.LINK_STATUS);
                if (!linked) {
                    var error = gl.getProgramInfoLog(shaderProgram);
                    alert("Error in program linking: " + error);
                    return;
                }

                gl.useProgram(shaderProgram);

                aVertexPosition = gl.getAttribLocation(shaderProgram,
                        "aVertexPosition");
                aVolumePosition = gl.getAttribLocation(shaderProgram,
                        "aVolumePosition");
                gl.enableVertexAttribArray(aVertexPosition);
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

    function currentScales() {
        return {
            x : Math.max(1.0, canvas.width / canvas.height),
            y : Math.max(1.0, canvas.height / canvas.width)
        };
    }

    function redraw() {
        canvas.width = Math.round(canvas.clientWidth * supersample);
        canvas.height = Math.round(canvas.clientHeight * supersample);
        gl.viewport(0, 0, canvas.width, canvas.height);
        var scales = currentScales();

        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

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
        mult(center.get(), scales.x, axisX, -scales.y, axisY), //
        mult(center.get(), -scales.x, axisX, -scales.y, axisY), //
        mult(center.get(), scales.x, axisX, scales.y, axisY), //
        mult(center.get(), -scales.x, axisX, scales.y, axisY));
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

                            var screenScales = currentScales();
                            var scaleX = -2.0 * screenScales.x
                                    / canvas.offsetWidth;
                            var scaleY = 2.0 * screenScales.y
                                    / canvas.offsetHeight;
                            var oldC = center.get();
                            var newC = mult(oldC, delta[0] * scaleX, axisX,
                                    delta[1] * scaleY, axisY);
                            center.set(newC);
                        }
                    });

    $(canvas).mouseup(function(e) {
        oldPos = null;
    });

    $(canvas).parent().find(".maximize-button").click(function(e) {
        e.preventDefault();
        if (maximized) {
            $(canvas).parent().removeClass('maximized');
            center.redrawAll();
        } else {
            $(canvas).parent().addClass('maximized');
            redraw();
        }
        maximized = !maximized;
    });
}

$(document).ready(
        function() {

            var redrawFns = [];
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
                },
                redrawAll : function() {
                    $(redrawFns).each(function(idx, fn) {
                        fn();
                    });
                }
            };

            $("canvas").each(function(idx, elem) {
                var id = $(elem).attr('id');
                var axisX = lookupAxis(id[id.length - 2]);
                var axisY = lookupAxis(id[id.length - 1]);
                attachToCanvas(elem, axisX, axisY, center);
            });

            $(window).resize(center.redrawAll);
            $(window).bind('hashchange', center.redrawAll);
        });
