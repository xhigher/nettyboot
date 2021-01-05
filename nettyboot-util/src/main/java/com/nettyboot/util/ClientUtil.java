package com.nettyboot.util;

import com.nettyboot.config.ClientPeer;
import com.nettyboot.config.ClientType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientUtil {

	private static final Pattern PEERID_PATTERN = Pattern.compile("^(\\d{2})(\\w)\\w{10}(\\d)(\\w{8})(\\d)\\w{9}$");

	public static ClientPeer checkPeerid(String peerid){
		ClientPeer clientPeer = new ClientPeer();
		if(peerid != null){
			clientPeer.setPeerid(peerid);
			try{
				Matcher matcher = PEERID_PATTERN.matcher(peerid);
				if(matcher.matches()){
					clientPeer.setType(ClientType.of(Integer.parseInt(matcher.group(1))));
					clientPeer.setFlag(Integer.parseInt(matcher.group(2)));
					int rn = Integer.parseInt(matcher.group(3));
					int mn = Integer.parseInt(matcher.group(5));
					String ts36 = matcher.group(4);
					long ts = Long.valueOf(ts36, 36);
					if(ts % rn == mn) {
						clientPeer.setError(false);
					}
				}
			}catch (Exception e){
			}
		}
		return clientPeer;
	}

	public static String createPeerid(ClientPeer clientPeer){
		String peerid = null;
		if(clientPeer != null){
			String noice = StringUtil.randomString(19, true);
			long ts = System.currentTimeMillis();
			long rn = (long) Math.floor(Math.random() * 9) + 1;
			long mn = ts % rn;
			StringBuilder sb = new StringBuilder();
			sb.append(clientPeer.type().id());
			sb.append(clientPeer.flag());
			sb.append(noice, 0, 10);
			sb.append(rn);
			sb.append(Long.toString(ts, 36));
			sb.append(mn);
			sb.append(noice.substring(10));
			peerid = sb.toString();
		}
		return peerid;
	}

}