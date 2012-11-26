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
import org.ccnx.ccn.CCNContentHandler;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.security.keys.BasicKeyManager;
import org.ccnx.ccn.io.content.ContentDecodingException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;



public class RenameOutgoing implements CCNInterestHandler, CCNContentHandler{

	
	private CCNHandle incomingCCNHandle;
	private CCNHandle outgoingCCNHandle;
	
	private ContentName _nameA, _nameB;
	private BasicKeyManager bkm;
	
	public RenameOutgoing(ContentName tNameA, ContentName tNameB) throws ConfigurationException, IOException {
		incomingCCNHandle = CCNHandle.open();
		outgoingCCNHandle = CCNHandle.open();
		
		_nameA = tNameA;
		_nameB = tNameB;
		
		incomingCCNHandle.registerFilter(_nameA, this);
		System.out.println("Registered " + _nameA + " to be renamed to " + _nameB);
		
		bkm = new BasicKeyManager();
		bkm.initialize();
		System.out.println("Connected to BasicKeyManager");		
	}

	@Override
	public boolean handleInterest(Interest intrst) {
		// TODO Auto-generated method stub
		System.out.println("Received original Interest: " + intrst.name());
		intrst.name(_nameB.append(intrst.name()));
		
		System.out.println("Renamed it to: "+intrst.name());
		try {
			outgoingCCNHandle.expressInterest(intrst, this);
			System.out.println("Expressed the renamed Interest.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public Interest handleContent(ContentObject incCO, Interest intrst) {
		// TODO Auto-generated method stub
		try {
			System.out.println("Received encapsulated ContentObject: "+incCO.name());
			//WirePacket wp = new WirePacket();
		
			//wp.decode(incCO.content());
			//List<ContentObject> tempList = wp.data();
			//List<ContentObject> tempList = incCO;
			//if(tempList.size() == 1)
			//{
				ContentObject outCO = new ContentObject();
				outCO.decode( incCO.content() );
				System.out.println("Decapsulated "+outCO.name());
				incomingCCNHandle.put(outCO);
				System.out.println("Returned the original ContentObject");
			//}
			
		} catch (ContentDecodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

}
