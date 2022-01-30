import sounddevice as sd
import time
import numpy as np
import matplotlib.pyplot as plt


SOUND_AVERAGE_THRESHOLD = 0.0004
SOUND_AVERAGE_VALUES_AMOUNT = 50

def calculateLatestAverage(array):
    sum = 0.0
    for x in array:
        sum = sum + float(x)
    return sum / SOUND_AVERAGE_VALUES_AMOUNT



def updateLastValuesArray(array, newValue):
    array.pop(0)
    array.append(newValue)
    return array

def isSoundValueAnomaly(lastAverage, value):
	return lastAverage < (value - SOUND_AVERAGE_THRESHOLD) or lastAverage > (value + SOUND_AVERAGE_THRESHOLD)

def getSound(duration, fs):
    res =  sd.rec(int(duration * fs), samplerate=fs, channels=1, blocking=True, dtype='int16')



    res1 = []
    lastValues = []
    res2 = []
    for x in res:
        tmp = calculateSoundPressure(x[0])
        if (tmp > 0):
            res2.append(tmp)

    # for x in res:
	# 	if len(lastValues) < SOUND_AVERAGE_VALUES_AMOUNT:
	# 		lastValues.append(x[0])
	# 	else:
	# 		lastAverage = calculateLatestAverage(lastValues)
	# 		if not x[0] < 0:
	# 			if not isSoundValueAnomaly(lastAverage, x[0]):
	# 				res1.append(x[0])
	# 			lastValues = updateLastValuesArray(lastValues, x[0])
    # print(res2)

    # res3 = []
    # for i in range(0, len(res2), fs):
    #     if (i + fs > len(res2)):
    #         break
    #     tmp_res = res2[i:(i+50)]
    #     pA = max(tmp_res) - min(tmp_res)
    #     res_I = (pA**2)/(2*1.29*331)
    #     res_I = 10*np.log10(res_I*(10**8))
    #     res3.append(res_I)
    #     print()
    # # print(sum(res2) / len(res2))
    # # result = 10*np.log10(sum(res2) / len(res2))
    # print(res3)
    # # 20 log(A0 *2Vrms/32768/1mV/20μPa)

    result = sum(res2)/len(res2)
    # plt.plot(res2)
    # plt.show()
    return result, time.time()

    # return sum(res2) / len(res2), time.time()

def calculateSoundPressure(tmp_res):
    # return 20*np.log10(max(tmp_res)*2/32768/1/20) - 18
    tmp = np.abs(((tmp_res*1.0)/32768)/(0.00002))
    if (tmp == 0):
        return 0
    return np.abs(20*np.log10(tmp))

def filterSoundValue(lastValue, newValue):
	return (lastValue + newValue) / 2

def human_run():
    print(sd.query_devices())
    selected = int(input("Select wanted microphone: "))
    fs = int(sd.query_devices()[selected]['default_samplerate'])
    duration = 3
    getSound(duration, fs)

# human_run()