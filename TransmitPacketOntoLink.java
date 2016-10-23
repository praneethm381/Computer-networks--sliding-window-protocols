import java.util.LinkedList;
import java.util.Random;

class TransmitPacketOntoLink {
	private int nw_delay;
	private float dropProb;
	private LinkedList senderList = null;
	private LinkedList receiverList = null;
	private Random randFun = null;
	private long droppedpackets;
	private long ackPktsSent;
	private long ackPktsrcvd;
	private long dataPktsSent;
	private long dataPktsrcvd;

	TransmitPacketOntoLink(int networkDelay, 
			float inputPackDropProb, int inputPacketSize) {
		droppedpackets = 0L;
		ackPktsSent = 0L;
		ackPktsrcvd = 0L;
		dataPktsSent = 0L;
		dataPktsrcvd = 0L;
		nw_delay = networkDelay;
		dropProb = inputPackDropProb;
		senderList = new LinkedList();
		receiverList = new LinkedList();
		randFun = new Random();
		droppedpackets = 0L;
		ackPktsSent = 0L;
		ackPktsrcvd = 0L;
		dataPktsSent = 0L;
		dataPktsrcvd = 0L;
	}

	/**
	 * check if the datapacket can be dropped or has any error and take the
	 * current time as the injection time on to link
	 * 
	 * @param currentTime
	 * @param infoPacket
	 */
	public void addInfoPacketTOSen(long currentTime, Packet infoPacket) {
//everytime adding packet to the last of the list
		dataPktsSent += 1L;
		if (packetisDropped()) {
			System.out.println("DATA packet was dropped with seqNum="
					+ infoPacket.seqNum());
			return;
		}
		infoPacket.setInjectionTime(currentTime);

		senderList.addLast(infoPacket);
	}

	public Infopacket sendInfoPacket(long currentTime) {
		Infopacket infopacket = null;
		
		//everytime sending first packet of the window to receiver
		if (senderList.size() != 0) {
			infopacket = (Infopacket) senderList.removeFirst();
			if (currentTime >= infopacket.injectionTime()
					+ nw_delay - 1L) {
				dataPktsrcvd += 1L;
				return infopacket;
			}
			senderList.addFirst(infopacket);
			infopacket = null;
		}
		return infopacket;
	}

	public void addAckPacketToReceiver(long currentTime, Packet ackPacket) {

		//adding packet drop in the reverse direction also, only to detect number of seq numbers needed for comparison
		//remaining time i am keeping it commented
		ackPktsSent += 1L;

		ackPacket.setInjectionTime(currentTime);

		receiverList.addLast(ackPacket);
	}

	public AckPacket sendAckPacket(long currentTime) {

		AckPacket ackPacket = null;
		if (receiverList.size() != 0) {
			ackPacket = (AckPacket) receiverList
					.removeFirst();
			if (currentTime >= ackPacket.injectionTime() + nw_delay
					- 1L) {
				ackPktsrcvd += 1L;
				return ackPacket;
			}
			receiverList.addFirst(ackPacket);
			ackPacket = null;
		}
		return ackPacket;

	}

	/**
	 * using the idea of randomly picking dropped packets(probability)
	 * 
	 * @return
	 */
	private boolean packetisDropped() {
		float randomProb = randFun.nextFloat();
		if (randomProb < dropProb) {
			droppedpackets += 1L;
		}
		return randomProb < dropProb;
	}

	public long droppedpackets() {
		return droppedpackets;
	}

	public long ackPktsSent() {
		return ackPktsSent;
	}

	public long ackPktsrcvd() {
		return ackPktsrcvd;
	}

	public long dataPktsSent() {
		return dataPktsSent;
	}

	public long dataPktsrcvd() {
		return dataPktsrcvd;
	}
}
