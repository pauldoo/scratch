#!/usr/bin/env python
# coding: utf-8

import argparse
import re
import logging
import RPi.GPIO as GPIO
import time
import tweepy

import twittersecrets as tws

logging.basicConfig(level=logging.INFO)


BEER_PROBE = "28-0114b80b74ff"
ROOM_PROBE = "28-0114b80a4bff"
TARGET_TEMPERATURE = 25

def readTemperature(probename):
    filename = "/sys/bus/w1/devices/{0}/w1_slave".format(probename)
    logging.info("Reading probe {0} from file {1}".format(probename, filename))
    regex = "^.* crc=.* YES\n.* t=([0-9]+)$"
    with open(filename) as probefile:
        contents = probefile.read()
        match = re.match(regex, contents)
        if match:
            temperature = int(match.group(1)) / 1000.0
            logging.info("Probe has reading: {0} C".format(temperature))
            return temperature

    raise "Unable to read probe {0}".format(probename)

def readBeerTemperature():
    return readTemperature(BEER_PROBE)

def readRoomTemperature():
    return readTemperature(ROOM_PROBE)

def shouldHeaterBeOn(beerTemperature):
    result = (beerTemperature < TARGET_TEMPERATURE)
    logging.info("Beer is at {0}, target is {1}, heater should be on: {2}".format(beerTemperature, TARGET_TEMPERATURE, result))
    return result

def sendHeaterSignal(heaterOn):
    try:
        # set the pins numbering mode
        GPIO.setmode(GPIO.BOARD)

        # Select the GPIO pins used for the encoder K0-K3 data inputs
        GPIO.setup(11, GPIO.OUT)
        GPIO.setup(15, GPIO.OUT)
        GPIO.setup(16, GPIO.OUT)
        GPIO.setup(13, GPIO.OUT)

        # Select the signal to select ASK/FSK
        GPIO.setup(18, GPIO.OUT)

        # Select the signal used to enable/disable the modulator
        GPIO.setup(22, GPIO.OUT)

        # Disable the modulator by setting CE pin lo
        GPIO.output (22, False)

        # Set the modulator to ASK for On Off Keying
        # by setting MODSEL pin lo
        GPIO.output (18, False)

        # Initialise K0-K3 inputs of the encoder to 0000
        GPIO.output (11, False)
        GPIO.output (15, False)
        GPIO.output (16, False)
        GPIO.output (13, False)

        if heaterOn:
            logging.info("sending code 1111 socket 1 on")
            GPIO.output (11, True)
            GPIO.output (15, True)
            GPIO.output (16, True)
            GPIO.output (13, True)
        else:
            logging.info("sending code 0111 Socket 1 off")
            GPIO.output (11, True)
            GPIO.output (15, True)
            GPIO.output (16, True)
            GPIO.output (13, False)

        # let it settle, encoder requires this
        time.sleep(0.1)
        # Enable the modulator
        GPIO.output (22, True)
        # keep enabled for a period
        time.sleep(0.25)
        # Disable the modulator
        GPIO.output (22, False)

    finally:
        GPIO.cleanup()

def sendTweetUpdate(enabled, beerTemperature, roomTemperature, heaterOn):
    message = "Room is {0} °C. Beer is {1} °C. Ideal temperature is {2} °C. Heater is on: {3}. (this is a test)".format(roomTemperature, beerTemperature, TARGET_TEMPERATURE, heaterOn)
    logging.info("Considering tweet: {0}".format(message))

    if enabled:
        auth = tweepy.OAuthHandler(tws.consumer_key, tws.consumer_secret, secure=True)
        auth.set_access_token(tws.access_token, tws.access_token_secret)
        api = tweepy.API(auth)
        status = api.update_status(message)

        logging.info("Twitter status posted: {0}".format(status))
    else:
        logging.info("Tweeting not enabled.")

def main():
    logging.info("starting")
    
    parser = argparse.ArgumentParser()
    parser.add_argument("--tweet", help="enable posting to twitter", action="store_true")
    args = parser.parse_args()

    logging.info("Tweet enabled: {0}".format(args.tweet))    

    beerTemperature = readBeerTemperature()
    heaterOn = shouldHeaterBeOn(beerTemperature)
    roomTemperature = readRoomTemperature()
    sendHeaterSignal(heaterOn)

    sendTweetUpdate(args.tweet, beerTemperature, roomTemperature, heaterOn)

if __name__ == "__main__":
    try:
        main()
    except:
        logging.exception("Unexpected error")

