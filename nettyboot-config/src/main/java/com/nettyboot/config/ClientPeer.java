package com.nettyboot.config;
import java.io.Serializable;

public class ClientPeer implements Serializable {

	private static final long serialVersionUID = 2776539817759418128L;

	private String peerid = null;
	private ClientType type = null;
	private int flag = 0;
	private boolean error = true;

	public ClientPeer(){
		this.peerid = "";
		this.error = true;
		this.type = ClientType.unknown;
		this.flag = 0;
	}

	public void setPeerid(String peerid){
		this.peerid = peerid;
	}

	public void setType(ClientType type){
		this.type = type;
	}

	public void setFlag(int flag){
		this.flag = flag;
	}

	public void setError(boolean error){
		this.error = error;
	}

	public String peerid() {
		return this.peerid;
	}

	public boolean error() {
		return this.error;
	}

	public ClientType type() {
		return this.type;
	}

	public int flag() {
		return this.flag;
	}

}