import cherrypy, urllib2, hashlib, time, threading, sys
import os, os.path
import json
import subprocess

salt = "COMPSYS302-2016"
IP = "172.23.72.48" #The IP that is 
listen_port = 19950 #your port
listen_ip = "172.23.72.48"
location = "1"

versusaddress = "http://172.23.72.48:19951" #the address of the person you are versing


class MainApp(object):
	username = "bananas" #change#
	password = "bananas" #change#
	hashcode = "bananas" #change#
	names, locations, ips, ports, thingy = [], [], [], [], []
	loggedIn = False

	"""Variables contain resources"""
	header = "content not loaded"
	header1 = "content not loaded"
	footer = "content not loaded"
	login = "content not loaded"
	data = "content not loaded"
	challenge_state = 0
	challengers = [] #Opposing users challenged client
	challenge_request, status = [], [] #Client Challenged opposing users]
	

	#CherryPy Configuration
	_cp_config = {'tools.encode.on': True, 
                  'tools.encode.encoding': 'utf-8',
                  'tools.sessions.on' : 'True',
                 }                 

	# If they try somewhere we don't know, catch it here and send them to the right place.
	@cherrypy.expose
	def default(self, *args, **kwargs):
		"""The default page, given when we don't recognise where the request is for."""
		Page = "I don't know where you're trying to go, so have a 404 Error."
		cherrypy.response.status = 404
		return Page

    # PAGES (which return HTML that can be viewed in browser)
	@cherrypy.expose
	def index(self):
		if (self.header == "content not loaded"):
			self.load_Resources() 
		Page = self.header
		###################################################
		if self.loggedIn:
			Page += "Hello " + self.username + "!"
		else:
			Page += "<div>Click here to <a href='log_in'>login</a></div>"
		####################################################
		Page += self.footer
		return Page
    
	@cherrypy.expose
	def log_in(self):
		if (self.header == "content not loaded"):
			self.load_Resources() 
		Page = self.header
		Page += self.logoff_gen(0)
		if(self.loggedIn):
			Page += "Please Log Out First."
		else:
			Page += self.login
		Page += self.footer
		return Page

	@cherrypy.expose
	def lobby(self):
		if (self.header == "content not loaded"):
			self.load_Resources() 
		Page = self.header1 #uses header with higher refresh rate
		Page += self.logoff_gen(1)
		if(self.loggedIn == False):
			Page += "<p>You need to log in first to access the Lobby</p>"
		else: #Constructs and prints data in form of table######################
			Page += "You can connect to users with location: " + location
			Page += '<table width = 60% id = "data">'
			Page += '<tr>'
			Page += "<th>UserName</th>"
			Page += "<th>Location</th>"
			#Page += "<th>IP Address</th>"
			#Page += "<th>Ports</th>"
			#Page += "<th>Data</th></tr>"
			i = 0
			for name in self.names:
				Page += "<tr><td>" + name + "</td>"
				Page += "<td>" + self.locations[self.names.index(name)] + "</td>"
				#Page += "<td>" + self.ips[self.names.index(name)] + "</td>"
				#Page += "<td>" + self.ports[self.names.index(name)] + "</td>"
				#Page += "<td>" + self.thingy[self.names.index(name)] + "</td>"
				if(self.names.index(name) != len(self.names) - 1):
					Page += '<td> <a href="send_challenge?username=' + name + '">Challenge</a> </td>'
			Page += "</table>"
		#########Constructs and prints challenge request#######
		Page += self.notification_Gen()
		
		Page += self.footer
		return Page

	@cherrypy.expose
	def notification_Gen(self):
		newpage = '<div id = "notifications"><h3>Notifications</h3>'
		
		#Testing
		#self.challenge_state = 3;
		#self.challenged_state = True;
		#test finished
		#self.challenge("Ethan")
		
		#Challenge request confirmation 0-none, 1-pending, 2-reject, 3-accepted 
		for username in self.challenge_request:
			newpage += '<div id = "notification1" class = "notificationunit"><p>' + username + '</p>'
			newpage += '<p> Challenge Status:'
			if(self.status[self.challenge_request.index(username)] == 3):
				newpage += 'Accepted</p></div>'
			elif(self.status[self.challenge_request.index(username)] == 2):
				newpage +='Rejected</p></div>'
			elif(self.status[self.challenge_request.index(username)] == 1):
				newpage += 'Pending</p></div>'


		#Challenge notification box    
		for username in self.challengers:
			newpage += '<div id="notification2" class="notificationunit">'
			newpage += '<div id = "text"><p>Challenge Request</p></div>'
			newpage += '<div id = "text"><p>'+ str(username) + '</p></div>'
			newpage += '<div id = "notification2buttons">' 
			newpage += "<a class='button' href='respond_challenge?username=" + str(username) + "&response=1'>Accept</a>"
			newpage += "<a class='button' href='respond_challenge?username=" + str(username) + "&response=0'>Reject</a>"
			newpage += '</div>'
			newpage += '</div>'
			
			
        #No notifications text
		if(newpage == '<div id = "notifications"><h3>Notifications</h3>'):
			newpage += '<div id = "notification3" class="notificationunit">'
			newpage += '<p>No Notifications</p>'
			newpage += '</div>'

		
		
		newpage += "</div>"

		return newpage
			
	
	#Opposing user challenges client
	#Displays information to client and waits on action before calling respond_challenge
	@cherrypy.expose
	@cherrypy.tools.json_in()
	def challenge(self, username="notdefined"):
		input_data = cherrypy.request.json
		username = input_data['username']
		if(not self.find(username, self.challengers)):
			self.challengers.append(username)
		return "0"
	
	
	#Opposing user responds to challenge request of client
	#Displays information to client and sets the game up for play
	@cherrypy.expose
	@cherrypy.tools.json_in()
	def respond(self, username="failedtoretrieveuser"):
		input_data = cherrypy.request.json
		username = input_data
		#If user accessing this API is not in challenge request set then return
		if(not self.find(username, self.challenge_request)):
			return
			
		#sets up the Ip address and port as global variable
		versusaddress = ips[self.names.index(username)] + ":" + ports[self.names.index(username)]
		
		raise cherrypy.HTTPRedirect("/startMaster")
		
		
		
	@cherrypy.expose		
	#Client responds to request of opposing user 0-rejected, 1-accepted
	def respond_challenge(self, username, response):
		
		
		if(response == "1"):
			#Display status of challenge as accepted
			
			#Set the IP address and port as global variable
			versusaddress = str(self.ips[self.names.index(username)]) + ":" + str(self.ports[self.names.index(username)])
			#Call Respond API of Opponent
			#Create Json package
			input_data = {"username": username, "response": response}
			data = json.dumps(input_data)
			nf = urllib2.Request("http://" + str(self.ips[self.names.index(username)]) + ":" + str(self.ports[self.names.index(username)]) + "/respond?",
				data, {'Content-Type':'application/json'})
			nf = urllib2.urlopen(nf)
			raise cherrypy.HTTPRedirect("/startSlave")
		
		
		
		if(response == "0"):
		#Challenge Rejected
		#Display status of challenge as rejected
			self.status[self.challengers.index(username)] = 2
		#Call respond API of opponent
			#Create Json package
			input_data = {"username": username, "response": response}
			data = json.dumps(input_data)
			nf = urllib2.Request("http://" + str(self.ips[self.names.index(username)]) + ":" + str(self.ports[self.names.index(username)]) + "/respond?",
				data, {'Content-Type':'application/json'})
		
		#Reload lobby
		Page = self.lobby()
		return Page
		
	@cherrypy.expose		
	#Client sends challenge to opposing user
	def send_challenge(self, username):
		#Add Challenger to request list
		if(not self.find(username, self.challenge_request)):
			self.challenge_request.append(username)
			self.status.append(1)
		#Create Json data package
		output_dict = {"username": self.username}
		data = json.dumps(output_dict) 
		#Send challenge request to opponent
		nf = urllib2.Request("http://" + str(self.ips[self.names.index(username)]) + ":" + str(self.ports[self.names.index(username)]) + "/challenge?",
		 data, {'Content-Type':'application/json'})
		nf = urllib2.urlopen(nf)
		
		#Reload the lobby
		Page = self.lobby()
		return Page
	
	
	
	#looks for element in list, returns true if the element is found
	def find(self, element, list):
		for elements in list:
			if(element == elements):
				return True
		
		return False
	

	
	def load_Resources(self):
		self.header = open('public/header.txt').read()
		self.header1 = open('public/header1.txt').read()
		self.footer = open('public/footer.txt').read()
		self.login = open('public/login.txt').read()
        
		return

	@cherrypy.expose
	def report(self, usernameTemp, passwordTemp):

		self.username = usernameTemp
		self.password = passwordTemp
		self.hashcode = self.hashPassword()

		nf = urllib2.Request("http://cs302.pythonanywhere.com/report?username="+self.username+"&password="+self.hashcode+"&ip="+IP+"&port="+str(listen_port)+"&location="+location)
		nf = urllib2.urlopen(nf)
		html = nf.read()

		if (html[0] == "0"): #changes#
			html += "<br/><br/>Click here to <a href='getList'>getList</a>."
			self.loggedIn = True
			self.getList()
			html = self.lobby()
			self.update() #initialize looping thread#
		else:
			self.loggedIn = False
			#html = self.log_in()
		return html


	def getList(self):
		nf = urllib2.Request("http://cs302.pythonanywhere.com/getList?username="+self.username+"&password="+self.hashcode)
		nf = urllib2.urlopen(nf)
		html = nf.read()
		self.data = html #Stores data in class
		#self.data_f = self.separate(html) #format data
		self.extract(html)
		self.lobby()
		return

	def extract(self, data_string):
		#assumes code 0
		users = []
		data = data_string[30:]
		#local variables initialised
		names, locations, ips, ports, thingy = [], [], [], [], []
		
		end = data.find("\n") + 1
		while (end > 0):
			users.append(data[0:end])
			data = data[end:]
			end = data.find("\n") + 1
			print "data:"
			print data
			print end
		users.append(data)
		
		for user in users:			
			end = user.find(",")
			names.append(user[0:end])
			user = user[end+1:]
			
			end = user.find(",")
			locations.append(user[0:end])
			user = user[end+1:]
			
			end = user.find(",")
			ips.append(user[0:end])
			user = user[end+1:]
			
			end = user.find(",")
			ports.append(user[0:end])
			user = user[end+1:]
			
			end = user.find(",")
			thingy.append(user[0:end])
			user = user[end+1:]
			
		self.names = names
		self.locations = locations
		self.ips = ips
		self.ports = ports
		self.thingy = thingy
	
	@cherrypy.expose
	def logoff(self): #changes#
		nf = urllib2.Request("http://cs302.pythonanywhere.com/getList?username="+self.username+"&password="+self.hashcode)
		nf = urllib2.urlopen(nf)
		html = nf.read()

		self.username = "bananas"
		self.password = "bananas"
		self.hashcode = "bananas"
		self.loggedIn = False

		Page = self.index()
		return Page
	

	def logoff_gen(self, mode):
		Page = ''
		if(self.loggedIn and mode == 1):
			Page += "</div>"
			Page += "<a href='logoff' id='logoutbutton'>Log Out</a>"
			Page += "<div class='content1'>"
		
		if(self.loggedIn and mode ==0):
			Page += "</div>"
			Page += "<a href='logoff()' id='logoutbutton'>Log Out</a>"
			Page += "<div class='content'>"
			
		
		return Page

	@cherrypy.expose
	def ping(self, sender):
		return 0
		
	@cherrypy.expose
	def roundtrip(self, sender):
		
		url = "http://" + str(self.ips[self.names.index(sender)]) + ":" + str(self.ports[self.names.index(sender)]) + "/ping?" + self.username

		
		ping = subprocess.Popen(["ping.exe",url], stdout = subprocess.PIPE)
		print(ping.communicate()[0])
		
	
	def hashPassword(self):
		try:
			hashPassword = hashlib.sha256(self.password+salt).hexdigest()
		except TypeError:
			hashPassword = "bananas"
		return hashPassword

	def update(self): #changes#
		global t, location, IP, listen_port
		if self.loggedIn:
			status = "Server refresh attempt...\n"
			nf = urllib2.Request("http://cs302.pythonanywhere.com/report?username="+self.username+"&password="+self.hashcode+"&ip="+IP+"&port="+str(listen_port)+"&location="+location)
			nf = urllib2.urlopen(nf)
			status += nf.read() + "\n"
			print status
			t = threading.Timer(30, self.update)
			t.start()

	@cherrypy.expose
	def listAPI(self):
		data = "/<listAPI>[<self>]\n"
		data += "/<ping>[<self>][<username_string>]\n"
		data += "/<challenge>[<self>][<username_string>]\n"
		data += "/<respond>[<self>][<username_string>][<response>]\n"
		data += "/<rules>[<self>][<username_string>]\n"
		data += "/<setControls>[<self>][<input>]\n"
		data += "/<recieveKey>[<self>][<Username_string>][<keyType>][<value>]\n"
		data += "/<setState>[<self>][<sender>][<state>]"
		
		
		data += "Encoding<JSON>\n"
		data += "Encryption<>\n"
		data += "Hashing<SHA256>\n"
		
		return data
	
	@cherrypy.tools.json_in()
	@cherrypy.expose
	def rules(self, username_string = "none"):
		input_data = cherrypy.request.json
		username_string = input_data
		
		data = "Server rules are as follows:\n"
		data += "FPS: 30 \n"
		data += "Rotation speed: 160 degrees per second\n"
		data += "Interaction With Walls: Obstruction of any objects\n"
		data += "Bullet Bouncing Behaviour: Bullets continue bouncing and last for 2.7sec\n"
		data += "Spawning of Power-Ups: Power ups are spawned randomly with an average of 2 Per 30 seconds\n"
		data += "Spawning of Tanks: Tanks are spawned randomly in areas without walls\n"
		data += "Spawning of Bullets: Bullets are spawned directly in front of the tank that shoots them\n"
		data += "Spawning of Walls: Walls are generated in a fixed format before the game begins\n"
		
		package = {"rules": data}
		package = json.dumps(package)
		
		return package
		
	@cherrypy.expose    
	def test(self): #All inputs are strings by default
		config = {
			'/': {
				'tools.sessions.on': True,
				'tools.staticdir.root': os.path.abspath(os.getcwd())
			},
			'/static': {
				'tools.staticdir.on': True,
				'tools.staticdir.dir': './public'
			}
		}
		cherrypy.tree.mount(Master(), "/", config)
		raise cherrypy.HTTPRedirect('/')
		
	@cherrypy.expose    
	def startMaster(self): #All inputs are strings by default
		config = {
			'/': {
				'tools.sessions.on': True,
				'tools.staticdir.root': os.path.abspath(os.getcwd())
			},
			'/static': {
				'tools.staticdir.on': True,
				'tools.staticdir.dir': './public'
			}
		}
		cherrypy.tree.mount(Master(), "/", config)
		raise cherrypy.HTTPRedirect('/')
				
	@cherrypy.expose    
	def startSlave(self): #All inputs are strings by default
		config = {
			'/': {
				'tools.sessions.on': True,
				'tools.staticdir.root': os.path.abspath(os.getcwd())
			},
			'/static': {
				'tools.staticdir.on': True,
				'tools.staticdir.dir': './public'
			}
		}
		cherrypy.tree.mount(Slave(), "/", config)
		raise cherrypy.HTTPRedirect('/')

	@cherrypy.expose
	def quit(self):
		t.cancel()
		sys.exit()

### Thread for automated reporting...
t = threading.Timer(2, MainApp.update)

### Class for running the game as master.

class Master(object):
	p1Score = 0
	p2Score = 0
	gameOver = False
	controls = '00000'

    #CherryPy Configuration
	_cp_config = {'tools.encode.on': True, 
					'tools.encode.encoding': 'utf-8',
					'tools.sessions.on' : 'True',
					}
					
	@cherrypy.expose
	def default(self, *args, **kwargs):
		"""The default page, given when we don't recognise where the request is for."""
		Page = "I don't know where you're trying to go, so have a 404 Error."
		cherrypy.response.status = 404
		return Page

	@cherrypy.expose
	def index(self):
		page = "Currently in game (as master server)\n"
		page += "Click here to <a href='gameOver'>seeResults</a>." #testing
		threading.Timer(0.1, self.inGame).start()
		return page
		
	# used to open the .jar file
	def openGame(self):
		subprocess.call(['java', '-jar', 'MasterTanksOnline.jar'])
	
	def inGame(self):
		global vsAddress, username
		
		server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		server_socket.bind(("", 5001))
		server_socket.listen(5)

		print "TCPServer Waiting for client on port 5001"

		# open the java game
		threading.Timer(0.1, self.openGame).start()

		client_socket, address = server_socket.accept()
		print "I got a connection from ", address
		sentBytes = client_socket.send ("0"+'\r\n')

		data = client_socket.recv(512)
		print "RECIEVED:" , data			
		while (data != "0\r\n"):
			parameters = data.split(',')
			start = urllib2.Request(vsAddress+"/receiveObject?sender="+username+"&objectID="+parameters[0]+"&objType="+parameters[1]+
				"&x="+parameters[2]+"&y="+parameters[3]+"&angle="+parameters[4]+"&state="+parameters[5]+"&alive="+parameters[6]+"")
			start = urllib2.urlopen(start)
			print start.read()
			sentBytes = client_socket.send ("0"+'\r\n')
			data = client_socket.recv(512)
			print "RECIEVED:" , data
			
		start = urllib2.Request(vsAddress+"/setState?sender="+username+"&state=1")
		start = urllib2.urlopen(start)
		print start.read()
		
		sentBytes = client_socket.send ("0"+'\r\n')
	
		while 1:
			rdata = client_socket.recv(512)
			if (rdata == "quit\r\n"):
				client_socket.send ("0\r\n")
				break;
			else:
				index = rdata.find(",")
				self.p1Score = int(rdata[:index])
				self.p2Score = int(rdata[index+1:])
				
			sentBytes = client_socket.send ("0"+'\r\n')
			
			sendPos = client_socket.recv(512)
			start = urllib2.Request(vsAddress+"/setPositions?sender="+username+"&positions="+sendPos)
			start = urllib2.urlopen(start)
			print sendPos

			sdata = self.controls
			sentBytes = client_socket.send (self.controls+'\r\n')
			if sentBytes == 0:
				raise RuntimeError("socket connection broken")
		return
		
	@cherrypy.expose
	def setControls(self,input):
		self.controls = input
		return "0"
		
	@cherrypy.expose
	def receiveKey(self,sender,keyType,value):
		if (keyType == '0'):
			self.l = value[0]
		elif (keyType == '1'):
			self.r = value[0]
		elif (keyType == '2'):
			self.u = value[0]
		elif (keyType == '3'):
			self.d = value[0]
		elif (keyType == '4'):
			self.s = value[0]
		return '0'
		
	@cherrypy.expose
	def gameOver(self):
		if (not self.gameOver): # TODO this isn't wooorking...
			page = "The game isn't over yet!"
		elif (self.p1Score > self.p2Score):
			page = "Congratulations, you won."
			page += "<p>Your score: " + str(self.p1Score) + "</p>"
			page += "<p>Your friend's score: " + str(self.p2Score) + "</p>"
		elif (self.p1Score == self.p2Score):
			page = "Everyone's a winner!"
			page += "<p>Your score: " + str(self.p1Score) + "</p>"
			page += "<p>Your friend's score: " + str(self.p2Score) + "</p>"
		else:
			page = "rekd"
			page += "<p>Your score: " + str(self.p1Score) + "</p>"
			page += "<p>Your friend's score: " + str(self.p2Score) + "</p>"
		page += "<p>Click here to <a href='goHome'>goHome</a>."
		return page
		
	@cherrypy.expose
	def goHome(self):
		config = {
			'/': {
				'tools.sessions.on': True,
				'tools.staticdir.root': os.path.abspath(os.getcwd())
			},
			'/static': {
				'tools.staticdir.on': True,
				'tools.staticdir.dir': './public'
			}
		}
		cherrypy.tree.mount(MainApp(), "/", config)
		raise cherrypy.HTTPRedirect('/')

		
#################### Slave

class Slave(object):
	controls = "00000"
	state = 0
	client_socket = None
	sendPositions = "1,150,250,0/2,900,340,0"
	p1Score = 0
	p2Score = 0

    #CherryPy Configuration
	_cp_config = {'tools.encode.on': True, 
					'tools.encode.encoding': 'utf-8',
					'tools.sessions.on' : 'True',
					}
					
	@cherrypy.expose
	def default(self, *args, **kwargs):
		"""The default page, given when we don't recognise where the request is for."""
		Page = "I don't know where you're trying to go, so have a 404 Error."
		cherrypy.response.status = 404
		return Page

	@cherrypy.expose
	def index(self):
		page = "Currently in game (as slave server)\n"
		page += "Click here to <a href='gameOver'>seeResults</a>." #testing
		threading.Timer(0.1, self.inGame).start()
		return page

# used to open the .jar file
	def openGame(self):
		subprocess.call(['java', '-jar', 'MasterTanksOnline.jar'])
	
	def inGame(self):
		global vsAddress, username
		
		server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		server_socket.bind(("", 5000))
		server_socket.listen(5)

		print "TCPServer Waiting for client on port 5000"

		# open the java game
		threading.Timer(0.1, self.openGame).start()

		self.client_socket, address = server_socket.accept()
		print "I got a connection from ", address
		sentBytes = self.client_socket.send ("0"+'\r\n')
		
		while (self.state == 0):
			pass
			#wait
			
		sentBytes = self.client_socket.send ("start"+'\r\n')
		print "started!"
		while 1:
			rdata = self.client_socket.recv(512)
			self.controls = rdata[:5]
			print self.controls
			
			start = urllib2.Request(vsAddress+"/setControls?input="+self.controls)
			start = urllib2.urlopen(start)
			
			sentBytes = self.client_socket.send (self.sendPositions+'\r\n')
		return
		
	# Our own function for the master to define where the 2 tanks go, and where bullets are.
	@cherrypy.expose
	def setPositions(self,sender,positions):
		self.sendPositions = positions
		return "0"
	
	@cherrypy.expose
	def getKey(self,sender,keyType):
		errorCode = "0"
		try:
			return errorCode + "," + controls[keyType]
		except IndexError:
			return "1"
	
	@cherrypy.expose
	def setState(self,sender,state):
		self.state = int(state)
		errorCode = "0"
		return errorCode
		
	@cherrypy.expose
	def receiveObject(self,sender,objectID,objType,x,y,angle,state,alive):
		object = objectID+","+objType+","+x+","+y+","+angle+","+state+","+alive+'\r\n'
		# send object to java
		self.client_socket.send (object)
		self.client_socket.recv(512)
		
		errorCode = "0"
		return errorCode
		
	@cherrypy.expose
	def gameOver(self):
		if (not self.gameOver): # TODO this isn't wooorking...
			page = "The game isn't over yet!"
		elif (self.p1Score > self.p2Score):
			page = "Congratulations, you won."
			page += "<p>Your score: " + str(self.p1Score) + "</p>"
			page += "<p>Your friend's score: " + str(self.p2Score) + "</p>"
		elif (self.p1Score == self.p2Score):
			page = "Everyone's a winner!"
			page += "<p>Your score: " + str(self.p1Score) + "</p>"
			page += "<p>Your friend's score: " + str(self.p2Score) + "</p>"
		else:
			page = "rekd"
			page += "<p>Your score: " + str(self.p1Score) + "</p>"
			page += "<p>Your friend's score: " + str(self.p2Score) + "</p>"
		page += "<p>Click here to <a href='goHome'>goHome</a>."
		return page
	
### Initialization
		
def runMainApp(config):
    # Create an instance of MainApp and tell Cherrypy to send all requests under / to it. (ie all of them)
    cherrypy.tree.mount(MainApp(), "/", config)

    # Tell Cherrypy to listen for connections on the configured address and port.
    cherrypy.config.update({'server.socket_host': listen_ip,
							'server.socket_port': listen_port,
                            'engine.autoreload.on': True,
                           })

    # Start the web server
    cherrypy.engine.start()

    # And stop doing anything else. Let the web server take over.
    cherrypy.engine.block()
 
 
#Run the function to start everything
if __name__ == '__main__':
    #Define configuration for running main instance
	#session support enabled
	#directory defined as ./public
	conf = {
		'/': {
			'tools.sessions.on': True,
			'tools.staticdir.root': os.path.abspath(os.getcwd())
		},
		'/static': {
			'tools.staticdir.on': True,
			'tools.staticdir.dir': './public'
		}
	}
	runMainApp(conf)
