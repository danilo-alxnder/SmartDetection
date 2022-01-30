import firebase_admin
from firebase_admin import credentials
from firebase_admin import db

# Fetch the service account key JSON file contents
cred = credentials.Certificate("project-mod8-firebase-adminsdk-jsaqz-d13f20ece0.json")
# Initialize the app with a service account, granting admin privileges
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://project-mod8.firebaseio.com/'
})
room = "Room6"
ref = db.reference('Rooms/Room1') #change Room1 to the wanted room and lastUpdate to property needs to change
tmp = ref.get()
ref1 = db.reference('Rooms/' + room)
ref1.set(tmp)
