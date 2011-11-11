all: hello-debug.js hello.js

clean:
	rm -rf *.js out

%.js: %.cljs
	cljsc $< '{:optimizations :advanced}' > $@

%-debug.js: %.cljs
	cljsc $< > $@

