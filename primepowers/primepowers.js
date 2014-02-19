(function() {

        var natsFrom = function(n) {
            return {
                val: n,
                next: function() { return natsFrom(n+1) }
            };
        }

        var filter = function(source, pred) {
            while (!pred(source.val)) {
                source = source.next();
            }
            return {
                val: source.val,
                next: function() { return filter(source.next(), pred); }
            }
        }

        var primeSeive = function(source) {
            var n = source.val;
            return {
                val: n,
                next: function() {
                    var pred = function(v) { return v % n != 0; };
                    return primeSeive(filter(source, pred));
                }
            }
        }

        var genPrimes = function() {
            return primeSeive(natsFrom(2));
        }

        var primeFactors = function(n, minPower) {
            if (n == 0) {
                return 0;
            }
            if (n == 1) {
                return 1;
            }
            var result = [];
            var primes = genPrimes();
            while (n != 1) {
                pow = 0;
                while (n % primes.val == 0) {
                    pow = pow + 1;
                    n = n / primes.val;
                }

                if (pow >= minPower) {
                    if (pow >= 2) {
                        pow = primeFactors(pow, minPower);
                    }
                    result.push([primes.val, pow]);
                }
                primes = primes.next();
            }
            return result;
        }

        var primeFactorsToTex = function(p) {
            if ($.isArray(p)) {
                var result = "";
                $.each(p, function(i, v) {
                        var fragment = v[0].toString() + "^{"  + primeFactorsToTex(v[1]) + "}";
                        result = result + "{" + fragment + "}";
                });
                return result;
            } else {
                return p.toString();
            }
        }

        var start = function() {

            var counter = 0;

            var tick = function() {
                var text1 = primeFactorsToTex(primeFactors(counter, 1));
                var text0 = primeFactorsToTex(primeFactors(counter, 0));
                var row = "<tr id='" + counter + "'><td>\\(" + counter + "\\)</td><td>\\(" + text1 + "\\)</td><td>\\(" + text0 + "\\)</td></tr>";
                $("table").append(row);
                MathJax.Hub.Queue(["Typeset",MathJax.Hub,counter.toString()]);
                counter = counter + 1;
                setTimeout(tick, 1000);
            }

            tick();

        }


        $(document).ready(start);


})();
