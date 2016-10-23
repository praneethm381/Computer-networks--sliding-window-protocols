public class Packet {

	private int seqNo;
	private long injectTime;

	// Data Packets
	private byte[] packetData = null;

	// Acknowledge Packets
	private String code = null;

	public String getCode() {
		return code;
	}

	public void setAckStatus(String code) {
		this.code = code;
	}

	public byte[] getIPacketData() {
		return packetData;
	}

	public void setIPacketData(int packetSize) {
		this.packetData = new byte[packetSize];
		;
	}

	public int seqNum() {
		return this.seqNo;
	}

	public void setSeqNum(int paramInt) {
		this.seqNo = paramInt;
	}

	public long injectionTime() {
		return this.injectTime;
	}

	public void setInjectionTime(long paramLong) {
		this.injectTime = paramLong;
	}

}

// For SR
class Infopacket extends Packet {
	private byte[] packetData = null;

	Infopacket(int paramInt) {
		this.packetData = new byte[paramInt];
	}
}

class AckPacket
extends Packet
{
private String code = null;

public AckPacket(String paramString)
{
  this.code = paramString;
}

public String type()
{
  return this.code;
}
}
