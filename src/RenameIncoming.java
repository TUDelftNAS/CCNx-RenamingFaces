/**
#``Renaming Faces for CCNx''
#
#Copyright (C) 2012, Delft University of Technology, Faculty of Electrical Engineering, Mathematics and Computer Science, Network Architectures and Services, Niels van Adrichem
#
#    This file is part of ``Renaming Faces for CCNx''.
#
#    ``Renaming Faces for CCNx'' is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License version 3 as published by
#    the Free Software Foundation.
#
#    ``Renaming Faces for CCNx'' is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with ``Renaming Faces for CCNx''.  If not, see <http:#www.gnu.org/licenses/>.
**/
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.KeyManager;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.config.SystemConfiguration;
import org.ccnx.ccn.io.content.ContentEncodingException;
import org.ccnx.ccn.protocol.CCNTime;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.SignedInfo;
import org.ccnx.ccn.protocol.SignedInfo.ContentType;



public class RenameIncoming implements CCNInterestHandler, CCNContentHandler {
	
	private CCNHandle incomingCCNHandle;
	private CCNHandle outgoingCCNHandle;
	//private BasicKeyManager bkm;
	private ContentName _name;

	public RenameIncoming(ContentName tName) throws ConfigurationException, IOException {
		incomingCCNHandle = CCNHandle.open();
		outgoingCCNHandle = CCNHandle.open();
		
		_name = tName;
		
		incomingCCNHandle.registerFilter(_name, this);
		System.out.println("Registered " + _name + " to be renamed to original names");
		
		//bkm = new BasicKeyManager();
		//bkm.initialize();
		//System.out.println("Connected to BasicKeyManager");		
		
		
	}

	@Override
	public boolean handleInterest(Interest intrst) {
		//System.out.println("Received renamed Interest: " + intrst.name());
		//intrst.name(new ContentName(_name.count(), intrst.name().count(), intrst.name().components()) );
		intrst.name(intrst.name().postfix(_name));
		//System.out.println("Renamed it back to: "+intrst.name());
	
		try {
			//outgoingCCNHandle.expressInterest(intrst, this);
			ContentObject co = outgoingCCNHandle.get(intrst, SystemConfiguration.getDefaultTimeout());
			if(co != null)
				incomingCCNHandle.put(renameCO(co));
			
			//outgoingCCNHandle.getNetworkManager().write(intrst);
			//System.out.println("Expressed the renamed Interest.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	private ContentObject renameCO(ContentObject incCO) throws InvalidKeyException, SignatureException, ContentEncodingException
	{
		//System.out.println("Received original ContentObject: "+incCO.name());
		
		KeyManager bkm = CCNHandle.getHandle().keyManager();
		SignedInfo si = new SignedInfo(bkm.getDefaultKeyID(), CCNTime.now(), ContentType.DATA, bkm.getDefaultKeyLocator());
		
		//System.out.println("Genereated ContentObject's signedInfo");
		
		ContentObject outCO = new ContentObject(_name.append(incCO.name()),si, incCO.encode(), bkm.getDefaultSigningKey());
		//System.out.println("Encapsulated it in: "+outCO.name());
		
		return outCO;
	}
	@Override
	public Interest handleContent(ContentObject co, Interest intrst) {
		try {
			incomingCCNHandle.put(renameCO(co));
			//System.out.println("Returned the encapsulated ContentObject.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return null;
	}

}
