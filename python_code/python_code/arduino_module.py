import serial
import time

MAX_TRIES = 1000


def decodeSerialLine(line):
    return line.decode().strip().strip('\x00')

def getDataFromArduino(num, ser):
	data = []
	for i in range(num):
		ser.write(b'Request Data')
		i = 0
		while i < MAX_TRIES:
			line = decodeSerialLine(ser.readline())
			if ("Result" in line):
				# print("Data from arduino: " + line)
				data.append(line)
				break
			else:
				time.sleep(0.01)
				i += 1
	
	lightArduino = []



	tempArduino = []




	for elem in data:
		res = elem.split("/")
		lightArduino.append(int(res[1]))
		tempArduino.append(float(res[2]))

	return lightArduino, tempArduino, time.time()