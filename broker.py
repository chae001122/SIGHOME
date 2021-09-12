import socket
import fcntl
import struct
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
import time

def on_connect(client, userdata, flags, rc):
    print("MQTT Broker Start")
    client.subscribe("/sensor/enter")
    client.subscribe("/sensor/bell")
    client.subscribe("/sensor/window")
    client.subscribe("/sensor/garage")
    client.subscribe("/sensor/fire")

    client.subscribe("/phone/turnoff/bell")
    client.subscribe("/phone/turnoff/window")
    client.subscribe("/phone/turnoff/enter")
    client.subscribe("/phone/turnoff/garage")
    client.subscribe("/phone/turnoff/fire")


def on_message(client, userdata, msg):
    if(msg.topic == '/sensor/enter'):
        GPIO.output(18, True)
        client.publish('/pause', 0)
        client.publish('/ras/enter', 0)

    elif(msg.topic == "/sensor/bell"):
        GPIO.output(27, True)
        client.publish('/pause', 0)
        client.publish('/ras/bell', 0)

    elif(msg.topic == "/sensor/window"):
        GPIO.output(22, True)
        client.publish('/pause', 0)
        client.publish('/ras/window', 0)

    elif(msg.topic == "/sensor/garage"):
        GPIO.output(23, True)
        client.publish('/pause', 0)
        client.publish('/ras/garage', 0)
    
    elif(msg.topic == "/sensor/fire"):
        GPIO.output(17, True)
        client.publish('/pause', 0)
        client.publish('/ras/fire', 0)


    elif(msg.topic == "/phone/turnoff/enter"):
        GPIO.output(18, False)
        print(msg)
        client.publish('/resume',0)
    elif(msg.topic == "/phone/turnoff/bell"):
        GPIO.output(27, False)
    elif(msg.topic == "/phone/turnoff/window"):
        GPIO.output(22, False)
    elif(msg.topic == "/phone/turnoff/fire"):
        GPIO.output(17, False)
        client.publish('/resume_garage',0)
    elif(msg.topic == "/phone/turnoff/garage"):
        GPIO.output(23, False)
        client.publish('/resume_garage',0)    

def get_ipaddress(network):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(
        s.fileno(),
        0x8915,
        struct.pack('256s',network[:15].encode('utf-8'))
        )[20:24])

port = 1883
GPIO.setmode(GPIO.BCM)
GPIO.setup(17, GPIO.OUT)
GPIO.setup(18, GPIO.OUT)
GPIO.setup(22, GPIO.OUT)
GPIO.setup(23, GPIO.OUT)
GPIO.setup(27, GPIO.OUT)
client = mqtt.Client('RasberryPI')
client.on_connect = on_connect
client.on_message = on_message
client.connect('192.168.0.6',port,60)
client.loop_forever()
print(-2)