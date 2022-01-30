import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
import argparse
import time
from datetime import datetime
import logging
import threading
import time
import concurrent.futures

from human_counting import *
from arduino_module import *
from sound_module import *

if __name__ == '__main__':
	#----------------------------------- Setup -----------------------------------

	ser = serial.Serial('/dev/cu.usbmodem14201', 9600, timeout=.1)
	time.sleep(2)

	parser = argparse.ArgumentParser()
	parser.add_argument("room", help="roomid for firebase",
						type=str)
	parser.add_argument("--testing", help="run a number of frame", action='store_true')
	parser.add_argument("--show", help="show live view", action='store_true')
	parser.add_argument("--live", help="live view", action='store_true')
	parser.add_argument("--frame", help="frame for each batch", type= int, default= 5)
	parser.add_argument("--interval", help="interval between update in seconds", type= int, default= 5)
	parser.add_argument("--firebase", help="flag for uploading to firebase", action='store_true')
	parser.add_argument("--arduino", help="get data from arduino", action='store_true')
	parser.add_argument("--recording", help="record audio", action='store_true')
	parser.add_argument("--threading", help="run parallel", action='store_true')


	args = parser.parse_args()
		# Fetch the service account key JSON file contents
	cred = credentials.Certificate("project-mod8-firebase-adminsdk-jsaqz-d13f20ece0.json")
	# Initialize the app with a service account, granting admin privileges

	#----------------------------------- Setup -----------------------------------
	firebase_admin.initialize_app(cred, {
		'databaseURL': 'https://project-mod8.firebaseio.com/'
	})
	cap = cv2.VideoCapture(int(0))
	print(sd.query_devices())
	selected = int(input("Select wanted microphone: "))
	# print(sd.query_devices()[selected])
	fs = int(sd.query_devices()[selected]['default_samplerate'])
	duration = 3
	#----------------------------------- Functions -----------------------------------

	if (args.threading):
		lastsoundValue = 0.0
		while(True):
			start_time = time.time()
			print("----New loop iteration------")

			executor = concurrent.futures.ThreadPoolExecutor(max_workers=3)
			peopleThread = executor.submit(webcam_frames, cap, args.frame, args.show)
			arduinoThread = executor.submit(getDataFromArduino, 4, ser)
			soundThread = executor.submit(getSound, duration, fs)

			print("submited all tasks")

			peopleResult, peopelRuntime = peopleThread.result()
			lightArduinoResult, tempArduinoResult, arduinoRuntime = arduinoThread.result()
			soundResult, soundRuntime = soundThread.result()

			print("finished all tasks")

			print()


			print("peopleThread result  : " + str(peopleResult))
			print("peopleThread runtime : " + str(peopelRuntime - start_time))
			print()
			
			print("arduinoThread result : " + str(lightArduinoResult) + "/" + str(tempArduinoResult))
			print("arduinoThread runtime: " + str(arduinoRuntime - start_time))
			print()
			lightRes = round(sum(lightArduinoResult)/len(lightArduinoResult))



			if lightRes < 230:
				lightState = "Dark"
			elif lightRes < 470:
				lightState = "Dim"
			elif lightRes < 535:
				lightState = "Light"
			else:
				lightState = "Bright"
			tempRes = round(sum(tempArduinoResult)/len(tempArduinoResult), 5)
			#----------------------Aparent Temperature----------------------------
			offset = 3.25																   # 5 increase T, 2 decreases it
			ws = 0                          									           #Wind Speed
			if peopleResult in range (0,3) and datetime.now().month in range (4,9):        #Warm months [4...9]
				rh = 0.64 * 0.832
			else:
				rh = 0.39 * 0.925

			if peopleResult in range (4,5) and datetime.now().month in range (4,9):
				rh = 0.55 * 0.842
			else:
				rh = 0.41 * 0.925

			if peopleResult > 5 and datetime.now().month in range (4,9):
				rh = 0.49 * 0.842
			else:
				rh = 43 * 0.842

			e = (rh/100)*6.105* 2.71**((17.27 *tempRes) / (237.7+tempRes))  #Water Vapour Pressure
			AparentTemp = tempRes+ 0.348 * e-0.70 * ws + (0.70* (0.002 * (ws+10)) - offset)

			print("soundThread result   : " + str(soundResult))
			print("soundThread runtime  : " + str(soundRuntime - start_time))
			print()

			if (args.firebase):
				print()
				print("Uploading to firebase")
				ref = db.reference('Rooms/'+ args.room +'/peopleCount')
				ref.set(peopleResult)

				ref = db.reference('Rooms/'+ args.room +'/lightCondition')
				ref.set(lightState)

				ref = db.reference('Rooms/'+ args.room +'/temperature')
				ref.set(AparentTemp)

				ref = db.reference('Rooms/'+ args.room +'/soundLevel')
				ref.set(soundResult)

				ref = db.reference('Rooms/'+ args.room +'/lastUpdate')

				ts = str(int(round(datetime.now().timestamp()*1000)))
				# print(ts)
				ref.set(ts)

				ref.set(ts)
				print("Uploaded to firebase")

			print()
			print("Runtime: " + str(time.time() - start_time))
			print("sleep for " + str(args.interval) + " seconds")
			time.sleep(args.interval)
			print()


		exit()
	if (args.live):
		webcam_detect(cap, True)
		cap.release()
		cv2.destroyAllWindows()
		exit()
	if (args.testing):
		print(webcam_frames(cap, args.frame, args.show))
	if (args.recording):
		myrecording = sd.rec(int(duration * fs), samplerate=fs, channels=1, blocking=True)
		plt.plot(myrecording)
		plt.show()
	if (args.interval):
		while(True):
			peopleCountRes = webcam_frames(cap, args.frame, args.show)
			print("Number of people (in " + str(args.testing) + " frames): "+ str(peopleCountRes))
			if (args.arduino):
				print("Requesting data from arduino")
				# for i  in range(10):
				# 	res = getDataFromArduino()
				# 	print(res)
				res = getDataFromArduino().split("/")
				print(res)
				lightRes = int(res[1])
				tempRes = float(res[2])
			if (args.firebase):
				print("Upload to firebase")
				ref = db.reference('Rooms/'+ args.room +'/peopleCount')
				ref.set(peopleCountRes)

				ref = db.reference('Rooms/'+ args.room +'/lightCondition')
				ref.set(lightRes)

				ref = db.reference('Rooms/'+ args.room +'/temperature')
				ref.set(tempRes)

				ref = db.reference('Rooms/'+ args.room +'/lastUpdate')

				ts = str(int(round(datetime.now().replace(tzinfo=timezone.utc).timestamp()*1000)))
				print(ts)
				ref.set(ts)
			print("sleep for " + str(args.interval) + " seconds")
			time.sleep(args.interval)
			print("-----------------")
	# webcam_detect(1, cap)

	cap.release()
	cv2.destroyAllWindows()