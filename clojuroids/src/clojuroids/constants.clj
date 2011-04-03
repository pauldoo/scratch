; :mode=clojure:

;; # Constants #

(ns clojuroids.constants)

;; Various constants used in the game.
;; *Linear Acceleration* values are in pixels per second per second.
;; *Angular Acceleration* values are in radians per second per second.
;; *Efficiency* values dictate the proportion of the object's velocity that remains after each passing second.

(def ^{:doc "The forward acceleration of the player when engines are firing."}
    acceleration 100.0)

(def ^{:doc "The angular acceleration of the player when they attempt to turn."}
    angular-acceleration 10.0)

(def ^{:doc "The player's efficiency value."}
    player-efficiency 0.7)

(def ^{:doc "The efficiency value for the bullets.  This is lower than the player's value to
    cause the bullets to straighten out fairly quickly"}
    bullet-efficiency 0.5)

(def ^{:doc "The forward acceleration of every bullet."}
    bullet-acceleration 200)

(def ^{:doc "The efficiency value for the sparkles that are given off by the player's engines
    and the bullets as they travel."}
    sparkle-efficiency 0.3)

(def ^{:doc "The velocity at which the sparkes spread in a random direction away from the
    emitting object.  This is a multiple of the emitting object's current acceleration."}
    sparkle-spread-velocity 0.25)

(def ^{:doc "The velocity at which the sparked kick backwards from the emitting object's
    current direction of acceleration.  This is a multiple of the emitting object's current acceleration."}
    sparkle-kick-velocity 1.0)

(def ^{:doc "Each accelerating object will emit roughly 'sparkle-amount * current-acceleration' sparkles per second."}
    sparkle-amount 0.3)

(def ^{:doc "Total number of sparkles permitted on the screen at once.  The limit is only
    present to keep performance good."}
    sparkle-limit 200)

(def ^{:doc "The radius of the smallest allowed asteroid."}
    smallest-asteroid 5.0)

(def ^{:doc "The radius of the staring asteroids."}
    initial-asteroid-size 30.0)

(def ^{:doc "Width of the playing field in pixels."}
    width 640)

(def ^{:doc "Height of the playing field in pixels."}
    height 480)

(def ^{:doc "The minimum delay (in seconds) between each bullet."}
    fire-delay 0.3)

(def ^{:doc "The rewind speed (in multiples of normal speed)."}
    rewind-speed 2.5)

(def ^{:doc "The slow-mo speed (in multiples of normal speed)."}
    slow-mo-speed 0.2)

