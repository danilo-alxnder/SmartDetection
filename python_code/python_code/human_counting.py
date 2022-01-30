import cv2
import numpy as np 
import time

def load_yolo():
	net = cv2.dnn.readNet("yolo-coco/yolov3.weights", "yolo-coco/yolov3.cfg")
	classes = []
	with open("yolo-coco/coco.names", "r") as f:
		classes = [line.strip() for line in f.readlines()]

	layers_names = net.getLayerNames()
	output_layers = [layers_names[i[0]-1] for i in net.getUnconnectedOutLayers()]
	colors = np.random.uniform(0, 255, size=(len(classes), 3))
	return net, classes, colors, output_layers


def display_blob(blob):
	'''
		Three images each for RED, GREEN, BLUE channel
	'''
	for b in blob:
		for n, imgb in enumerate(b):
			cv2.imshow(str(n), imgb)

def detect_objects(img, net, outputLayers):
	blob = cv2.dnn.blobFromImage(img, scalefactor=0.00392, size=(320, 320), mean=(0, 0, 0), swapRB=True, crop=False)
	net.setInput(blob)
	outputs = net.forward(outputLayers)
	return blob, outputs

def get_box_dimensions(outputs, height, width):
	boxes = []
	confs = []
	class_ids = []
	for output in outputs:
		for detect in output:
			scores = detect[5:]
			class_id = np.argmax(scores)
			conf = scores[class_id]
			if conf > 0.3:
				center_x = int(detect[0] * width)
				center_y = int(detect[1] * height)
				w = int(detect[2] * width)
				h = int(detect[3] * height)
				x = int(center_x - w/2)
				y = int(center_y - h / 2)
				boxes.append([x, y, w, h])
				confs.append(float(conf))
				class_ids.append(class_id)
	return boxes, confs, class_ids

def draw_labels(boxes, confs, colors, class_ids, classes, img, show):
    # print(confs)
    # print(class_ids)
	indexes = cv2.dnn.NMSBoxes(boxes, confs, 0.5, 0.4)
	font = cv2.FONT_HERSHEY_PLAIN
	person_cnt = 0
	for i in range(len(boxes)):
		if i in indexes:
			x, y, w, h = boxes[i]
			label = str(classes[class_ids[i]])
			if label != "person":
			    continue
			person_cnt += 1
			color = colors[i]
			cv2.rectangle(img, (x,y), (x+w, y+h), color, 2)
			cv2.putText(img, label + ": " + str(round(confs[i],3)), (x, y - 5), font, 1, color, 1)
	if show == True:
		# print("show: " + str(show))
		cv2.imshow("Image", img)
	return person_cnt


def webcam_frames(cap, frameNum, show):
	res = []
	for i in range(0, frameNum):
		model, classes, colors, output_layers = load_yolo()
		_, frame = cap.read()
		height, width, channels = frame.shape
		blob, outputs = detect_objects(frame, model, output_layers)
		boxes, confs, class_ids = get_box_dimensions(outputs, height, width)
		people_cnt = draw_labels(boxes, confs, colors, class_ids, classes, frame, show)
		key = cv2.waitKey(1)
		if key == 27:
		    break
		res.append(people_cnt)

	if (len(res) == 0):
		return 0

	return round(sum(res) / len(res)), time.time()

def webcam_detect(cap, show):
	model, classes, colors, output_layers = load_yolo()
	while True:
		_, frame = cap.read()
		height, width, channels = frame.shape
		blob, outputs = detect_objects(frame, model, output_layers)
		boxes, confs, class_ids = get_box_dimensions(outputs, height, width)
		people_cnt = draw_labels(boxes, confs, colors, class_ids, classes, frame, show)
		key = cv2.waitKey(1)
		if key == 27:
		    break