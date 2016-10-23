public class GBNImpl {

	// For GBN Sender
	private int packetsToSend;
	private int packetSize;
	private int networkDelay;
	private int packetsSent;

	private int windowSize;
	private int pktsSentInWindow;
	private int pktsToResend;
	private int packetsAckd;
	private Packet[] packetsInArray;
	private long[] timeouts;
	private long timeOfLastSend;

	// For GBN receiver
	private boolean flagIpacket;
	private int ipacketSeqNum;
	private int lastlySentSeqNum;

	// For common both sender and receiver
	private int noOfRetransmissions;
	private long sampleTimeout;

	Packet infoPacket = null; 
	Packet ackPacket = null;
	AckPacket actualAckPkt = null;
	Infopacket actualInfoPkt = null;
	
	public GBNImpl(int inputNumOfPackets, int inputPacketSize,
			int networkDelay, int senderWindowSize) {
		this.sampleTimeout = 5L;
		this.packetSize = inputPacketSize;
		this.windowSize = senderWindowSize;
		this.pktsToResend = 0;
		this.packetsAckd = 0;
		this.packetsToSend = inputNumOfPackets;
		this.pktsSentInWindow = 0;
		this.networkDelay = networkDelay;
		this.packetsSent = 0;

		this.timeOfLastSend = (-this.networkDelay);
		this.packetsInArray = new Packet[this.windowSize];
		this.timeouts = new long[this.windowSize];

		this.noOfRetransmissions = 0;
		
		// Receiver
		this.flagIpacket = false;
		this.lastlySentSeqNum = 0;
	}

	public void sendSenderInfo(long currentTime, GBNImpl gbn, TransmitPacketOntoLink transmitPacket) {
		infoPacket = gbn.buildInfoPacket(currentTime);
		if (infoPacket != null) {
			transmitPacket.addInfoPacketTOSen(currentTime, infoPacket);
		}
	}	

	public void sendSenderAck(long currentTime, GBNImpl gbn, TransmitPacketOntoLink transmitPacket) {
		ackPacket = gbn.buildAckPacket();
		if (ackPacket != null) {
			transmitPacket.addAckPacketToReceiver(currentTime, ackPacket);
		}
	}
	
	public void sendgbnInfoAndReceiveAtReceiver(long currentTime, GBNImpl gbn,
			TransmitPacketOntoLink transmitPacket) {
		actualInfoPkt = transmitPacket.sendInfoPacket(currentTime);
		if (actualInfoPkt != null) {
			gbn.receiveInfoPacket(actualInfoPkt);
		}
	}
	
	public void sendgbnAckAndReceiveAtSender(long currentTime, GBNImpl gbn,
			TransmitPacketOntoLink transmitPacket) {
		actualAckPkt = transmitPacket.sendAckPacket(currentTime);
		if (actualAckPkt != null) {
			gbn.receiveAckPacket(currentTime, actualAckPkt);
		}		
	}
	
	public Packet buildInfoPacket(long currentTime) {
	    Packet infoPacket = null;
	    if ((pktsSentInWindow > 0) && (currentTime > timeouts[0]))
	    {
	      pktsToResend = (pktsSentInWindow - 1);
	      pktsSentInWindow = 1;
	      noOfRetransmissions+=1;
	      infoPacket = packetsInArray[0];
	      timeouts[0] = (currentTime + 2 * networkDelay + sampleTimeout);
	        System.out.println("time:" + currentTime + " Sender: Retransmitting first DATA packet after timeout from start of the window. seqNum=" + infoPacket.seqNum());
	    }
	    else if (pktsToResend > 0)
	    {
	    	noOfRetransmissions+=1;
	    	infoPacket = packetsInArray[pktsSentInWindow];
	      timeouts[pktsSentInWindow] = (currentTime + 2 * networkDelay + sampleTimeout);
	      pktsSentInWindow += 1;
	      pktsToResend -= 1;
	        System.out.println("time:" + currentTime + " Sender: retransmitting DATA packet after packet timeout. seqNum=" + infoPacket.seqNum());
	    }else if (pktsSentInWindow < windowSize)
	    {
		      if ((packetsSent < packetsToSend) && (currentTime >= timeOfLastSend))
		      {
		      infoPacket = new Infopacket(packetSize);
		      infoPacket.setSeqNum(packetsSent + 1);
		        
		        packetsInArray[pktsSentInWindow] = infoPacket;
		        
		        timeouts[pktsSentInWindow] = (currentTime + 2 * networkDelay + sampleTimeout);
		        pktsSentInWindow += 1;
		        packetsSent += 1;
		        timeOfLastSend = currentTime;
		          System.out.println("time:" + currentTime + " Sender: transmitting DATA packet with seqNum=" + infoPacket.seqNum());
		      }
		    }

	    return infoPacket;
	  }

	public Packet buildAckPacket() {
	    AckPacket ackPacket = null;
	    if (flagIpacket)
	    {
	      ackPacket = new AckPacket("ACK");
	      ackPacket.setSeqNum(ipacketSeqNum);
	      flagIpacket = false;
	        System.out.println("       Receiver: transmitting ACK packet with seqNum=" + ipacketSeqNum);
	    }
	    return ackPacket;
	  }

	public void receiveAckPacket(long currentTime, Packet actualAckPkt) {
		
		//always we need to check first packet in the window is received or not
	      while ((actualAckPkt.seqNum() >= packetsInArray[0].seqNum()) && (pktsSentInWindow > 0) && (packetsAckd != packetsToSend))
	      {
	        for (int i = 0; i < pktsSentInWindow - 1; i++)
	        {
	          packetsInArray[i] = packetsInArray[(i + 1)];
	          timeouts[i] = timeouts[(i + 1)];
	        }
	        pktsSentInWindow -= 1;
	        if (pktsToResend > 0) {
	          pktsToResend -= 1;
	        }
	      }
	      if(packetsAckd > actualAckPkt.seqNum())
	      {
	    	  //lastpacketACKd = lastpacketACKd;
	      }else
	      {
	    	  packetsAckd = actualAckPkt.seqNum();
	      }
	        System.out.println("time:" + currentTime + " Sender: received ACK for packet with seqNum=" + actualAckPkt.seqNum());
	  }

	public void receiveInfoPacket(Packet actualInfoPkt) {
	    int i = 0;
	      i = lastlySentSeqNum;
	      lastlySentSeqNum += 1;
	      ipacketSeqNum = actualInfoPkt.seqNum();
	      if (ipacketSeqNum - lastlySentSeqNum >= 1)
	      {
	        ipacketSeqNum = i;
	        lastlySentSeqNum -= 1;
	      }
	      flagIpacket = true;
	        System.out.println("       Receiver: received a DATA packet with seqNum=" + actualInfoPkt.seqNum());
	  }

	/**
	 * If all the packets are sent to receiver and all acknowledgements are
	 * received at sender then return true
	 */
	public boolean allPacketsAreSent() {
		return (packetsAckd == packetsToSend)
				&& (packetsSent >= packetsToSend);
	}

	public long numOfRetransmissions() {
		return noOfRetransmissions;
	}
}
