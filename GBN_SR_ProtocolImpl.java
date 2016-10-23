public class GBN_SR_ProtocolImpl {

	public static void main(String[] inputParameters) {

		GBNImpl gbn = null;
		long currentTime = 0L;
		TransmitPacketOntoLink transmitPacket = null;

		SRImpl sr = null;

		if ((inputParameters.length != 7)) {
			System.out
					.println("Please use the below Syntax to simulate GBN and SR protocols");
			System.out.println("Syntax: Number_of_packets Alg_name Sender_window propagation_delay Data_rate Packet_size Packet_drop_probability");
			System.out.println("Example 1: 10 GBN 3 5 1000 1 0.05");
			System.out.println("Example 2: 10 SR 3 5 1000 1 0.05");
			System.exit(1);
		}

		int inputNumOfPackets = Integer.parseInt(inputParameters[0]);
		String inputAlgName = inputParameters[1];
		int senderWindowSize = Integer.parseInt(inputParameters[2]);
		int inputPropDelay = Integer.parseInt(inputParameters[3]);
		int inputDataRate = Integer.parseInt(inputParameters[4]);
		int inputPacketSize = Integer.parseInt(inputParameters[5]);
		float inputPackDropProb = Float.parseFloat(inputParameters[6]);
		
		int networkDelay = CalculateDelay(inputPropDelay, inputPacketSize,
				inputDataRate);

		transmitPacket = new TransmitPacketOntoLink(networkDelay,
				 inputPackDropProb, inputPacketSize);

		if (inputAlgName.equals("GBN")) {
			System.out.println("Starting GBN simulation with given parameters");
			gbn = new GBNImpl(inputNumOfPackets, inputPacketSize, networkDelay,
					senderWindowSize);

		} else if (inputAlgName.equals("SR")) {
			System.out.println("Starting SR simulation with given parameters");
			sr = new SRImpl(inputNumOfPackets, inputPacketSize, networkDelay,
					senderWindowSize);

		} else {
			System.out.println("Invalid argument. Error - unknown algorithm.");
			System.exit(1);
		}
		System.out.println("---------------------------------------------");
		System.out.println("Number of packets to send: "+inputNumOfPackets);
		System.out.println("Sender Window size: "+senderWindowSize);
		System.out.println("packet length: "+inputPacketSize + " bits");
		System.out.println("Packet drop probability: "+inputPackDropProb);
		System.out.println("----------------------------------------------");
		
		if (gbn != null) {

			while (!gbn.allPacketsAreSent()) {
				
				gbn.sendSenderInfo(currentTime,gbn,transmitPacket);
				
				gbn.sendSenderAck(currentTime,gbn,transmitPacket);

				gbn.sendgbnAckAndReceiveAtSender(currentTime,gbn,transmitPacket);

				gbn.sendgbnInfoAndReceiveAtReceiver(currentTime,gbn,transmitPacket);

				currentTime += 1L;
			}
		} else if (sr != null) {

			while (!sr.allPacketsAreSent()) {
				
				sr.sendSenderInfo(currentTime,sr,transmitPacket);
				
				sr.sendSenderAck(currentTime,sr,transmitPacket);

				sr.sendsrAckAndReceiveAtSender(currentTime,sr,transmitPacket);

				sr.sendsrInfoAndReceiveAtReceiver(currentTime,sr,transmitPacket);
				
				currentTime += 1L;
			}
		}

			System.out.println("----------------------------------------------------");
			System.out.println("Simulation done.");
			System.out.println("  Total time taken is: " + currentTime
					+ " millseconds");
			if (gbn != null)
				System.out
						.println("  Number of Retransmissions occured: "
								+ gbn.numOfRetransmissions());
			if (sr != null)
				System.out
						.println("  Number of Retransmissions occured: "
								+ sr.numOfRetransmissions());
			System.out.println("  Number of packets dropped: "
					+ transmitPacket.droppedpackets());
			System.out.println("  Number of ACK packets SENT by receiver and RECEIVED by sender: "
					+ transmitPacket.ackPktsSent()+"   "+transmitPacket.ackPktsrcvd());
			System.out.println("  Number of DATA packets SENT by sender and RECEIVED by the receiver: "
					+ transmitPacket.dataPktsSent()+"   "+transmitPacket.dataPktsrcvd());
	}

	/**
	 * Total delay is calculated as (propagation delay+transmission delay) where
	 * prop delay is taken as input parameter(msec) and transmission delay is
	 * calculated by formula (L/R) where L is length of packets and R is the
	 * network bandwidth.
	 */
	private static int CalculateDelay(int inputPropDelay, int inputPacketSize,
			int inputDataRate) {
		System.out.println("------------------------------------------------------");
		System.out.println("propagation delay is: "+inputPropDelay);
		System.out.println("transmission delay is: "+(double)inputPacketSize/ inputDataRate * 1000.0);
		int networkDelay = (int) (inputPropDelay + (double)inputPacketSize
				/ inputDataRate * 1000.0);
		System.out.println("network delay is: "+networkDelay);
		System.out.println("------------------------------------------------------");

		return networkDelay;

	}

}
