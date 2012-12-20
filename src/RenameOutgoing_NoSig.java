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
import org.ccnx.ccn.config.SystemConfiguration;
import org.ccnx.ccn.io.content.ContentDecodingException;
import org.ccnx.ccn.io.content.ContentEncodingException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.Interest;



public class RenameOutgoing_NoSig implements CCNInterestHandler, CCNContentHandler{

	
	private CCNHandle incomingCCNHandle;
	private CCNHandle outgoingCCNHandle;
	
	private ContentName _nameA, _nameB;
	//private BasicKeyManager bkm;
	
	public RenameOutgoing_NoSig(ContentName tNameA, ContentName tNameB) throws ConfigurationException, IOException {
		incomingCCNHandle = CCNHandle.open();
		outgoingCCNHandle = CCNHandle.open();
		
		_nameA = tNameA;
		_nameB = tNameB;
		
		incomingCCNHandle.registerFilter(_nameA, this);
		System.out.println("Registered " + _nameA + " to be renamed to " + _nameB);
		
		//bkm = new BasicKeyManager();
		//bkm.initialize();
		//System.out.println("Connected to BasicKeyManager");		
	}
	
	public void unRegister()
	{
		incomingCCNHandle.unregisterFilter(_nameA, this);
		outgoingCCNHandle.close();
		incomingCCNHandle.close();
	}

	@Override
	public boolean handleInterest(Interest intrst) {
		// TODO Auto-generated method stub
		//System.out.println("Received original Interest: " + intrst.name());
		intrst.name(_nameB.append(intrst.name()));
		
		//System.out.println("Renamed it to: "+intrst.name());
		try {
			//outgoingCCNHandle.expressInterest(intrst, this);
			ContentObject co = outgoingCCNHandle.get(intrst, SystemConfiguration.getDefaultTimeout());
			if(co != null)
				incomingCCNHandle.put(renameCO(co) );
			//System.out.println("Expressed the renamed Interest.");
		} catch (ContentDecodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	private ContentObject renameCO(ContentObject incCO) throws ContentDecodingException, ContentEncodingException
	{
		//System.out.println("Received original ContentObject: "+incCO.name());

		ContentName cn  = incCO.name();
		cn.decode( cn.postfix(_nameB).encode() );
		
		//System.out.println("Renamed it in: "+incCO.name());
		
		return incCO;
	}
	
	@Override
	public Interest handleContent(ContentObject co, Interest intrst) {
		// TODO Auto-generated method stub
	
			
			try {
				incomingCCNHandle.put(renameCO(co));
				//System.out.println("Returned the original ContentObject");
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
