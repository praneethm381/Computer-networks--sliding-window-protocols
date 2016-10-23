public class SRImpl {

	// For SR Sender
	private int packetsToSend;
	private int packetSize;
	private int networkDelay;
	
	private int[] status;
	private int successfulsent = 0;
	private int windowSize;
	private long[] timeouts;
	private Packet[] allPackets;
	private long timeOfLastSend;
	private int max;

	// receiver
	private boolean flagInfopacket;
	private int ipacketSeqNum;

	private int noOfRetransmissions;
	private long sampleTimeout;
	
	Packet infoPacket = null;
	Packet ackPacket = null;
	AckPacket actualAckPkt = null;
	Infopacket actualInfoPkt = null;

	public SRImpl(int inputNumOfPackets, int inputPacketSize, int networkDelay,
			int senderWindowSize) {

		// sender
		this.sampleTimeout = 5L;
		this.packetSize = inputPacketSize;
		this.networkDelay = networkDelay;
		this.packetsToSend = inputNumOfPackets;
		
		this.allPackets = new Packet[inputNumOfPackets];
		this.status = new int[inputNumOfPackets];
		this.timeouts = new long[inputNumOfPackets];
		this.windowSize = senderWindowSize;
		this.max = senderWindowSize;
		this.timeOfLastSend = (-this.networkDelay);
		
		// saving all packets in an array with its seqnumber and initial status
		// as 0
		for (int i = 0; i < inputNumOfPackets; i++) {
			this.allPackets[i] = new Infopacket(this.packetSize);
			this.allPackets[i].setSeqNum(i + 1);
			this.status[i] = 0;
		}
		this.noOfRetransmissions = 0;

		// for Receiver
		this.flagInfopacket = false;
	}
	
	public void sendSenderInfo(long currentTime, SRImpl sr,
			TransmitPacketOntoLink transmitPacket) {

		infoPacket = sr.buildInfoPacket(currentTime);
		if (infoPacket != null) {
			transmitPacket.addInfoPacketTOSen(currentTime, infoPacket);
		}
	}

	public void sendSenderAck(long currentTime, SRImpl sr,
			TransmitPacketOntoLink transmitPacket) {

		ackPacket = sr.buildAckPacket();
		if (ackPacket != null) {
			transmitPacket.addAckPacketToReceiver(currentTime, ackPacket);
		}
		
	}

	public void sendsrAckAndReceiveAtSender(long currentTime, SRImpl sr,
			TransmitPacketOntoLink transmitPacket) {
		actualAckPkt = transmitPacket.sendAckPacket(currentTime);
		if (actualAckPkt != null) {
			sr.receiveAckPacket(currentTime, actualAckPkt);
		}
		
	}

	public void sendsrInfoAndReceiveAtReceiver(long currentTime, SRImpl sr,
			TransmitPacketOntoLink transmitPacket) {
		actualInfoPkt = transmitPacket.sendInfoPacket(currentTime);
		if (actualInfoPkt != null) {
			sr.receiveInfoPacket(actualInfoPkt);
		}
		
	}

	// status = 0(intially), 1(sent), 2(in-process), 3(bad-ack)
	public Packet buildInfoPacket(long currentTime) {
	    int i = 0;
	    Packet localpacket = null;
	    if ((successfulsent < packetsToSend) && (currentTime >= timeOfLastSend)) {
	      for (; i < max; i++) {
	        if (status[i] != 1)
	        {
	          if (status[i] == 0)
	          {
	            localpacket = allPackets[i];
	            timeouts[i] = (currentTime + 2 * networkDelay + sampleTimeout);
	            timeOfLastSend = currentTime;
	            status[i] = 2;
	            System.out.println("time:" + currentTime + " Sender: transmitting info packet seqNum=" + localpacket.seqNum()); 
	            break;
	            
	          }
	          if ((status[i] == 3) || (currentTime > timeouts[i]))
	          {
	            localpacket = allPackets[i];
	            timeouts[i] = (currentTime + 2 * networkDelay + sampleTimeout);
	            timeOfLastSend = currentTime;
	            status[i] = 2;
	            System.out.println("time:" + currentTime + " Sender: retransmitting info packet seqNum=" + localpacket.seqNum());
	            break;
	          }
	        }
	      }
	    }
	    return localpacket;
	  }

	public void receiveAckPacket(long currentTime, Packet actualAckPkt) {
		  int packetsNumber = 0;
		  
		    for (; packetsNumber < max; packetsNumber++) {
		      if (allPackets[packetsNumber].seqNum() == actualAckPkt.seqNum()) {
		        break;
		      }
		    }
		      status[packetsNumber] = 1;
		      successfulsent += 1;
		        System.out.println("time:" + currentTime + " Sender: received ACK for packet with seqNum=" + actualAckPkt.seqNum());
		      int increaseWindowWith=0;
		      for (; increaseWindowWith < packetsToSend; increaseWindowWith++) {
		        if (status[increaseWindowWith] != 1) {
		          break;
		        }
		      }
		      max = (windowSize + increaseWindowWith);
		      if (max >= packetsToSend) {
		        max = packetsToSend;
		      }
		    
		    if (status[packetsNumber] != 1)
		    {
		        System.out.println("time:" + currentTime + " Sender: received a bad ACK for packet with seqNum=" + actualAckPkt.seqNum());
		      status[packetsNumber] = 3;
		    }
		  }

	public Packet buildAckPacket() {
		AckPacket localControlpacket = null;
		if (this.flagInfopacket) {
			localControlpacket = new AckPacket("ACK");
			localControlpacket.setSeqNum(ipacketSeqNum);
			this.flagInfopacket = false;
				System.out.println("       Receiver: transmitting ACK packet with seqNum="
						+ ipacketSeqNum);
		}
		return localControlpacket;
	}

	public void receiveInfoPacket(Packet actualInfoPkt) {
			ipacketSeqNum = actualInfoPkt.seqNum();
			flagInfopacket = true;
				System.out
						.println("       Receiver: received a DATA packet with seqNum="
								+ ipacketSeqNum);
		
	}

	public boolean allPacketsAreSent() {
		return successfulsent == packetsToSend;
	}

	public long numOfRetransmissions() {
		return noOfRetransmissions;
	}
}
