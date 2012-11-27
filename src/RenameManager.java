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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.profiles.ccnd.PrefixRegistrationManager;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;




public class RenameManager implements CCNInterestHandler {
	
	private SimpleResolver _resolver;
	private CCNHandle incomingCCNHandle;
	private Hashtable<String,Mapping> Mappings;
	
	public RenameManager() throws ConfigurationException, IOException
	{
		this(null);
	}
	
	public RenameManager(SimpleResolver rslvr) throws ConfigurationException, IOException {
		_resolver = rslvr;
		Mappings = new Hashtable<String,Mapping>();
		try {
			
			incomingCCNHandle = CCNHandle.open();
			//incomingCCNHandle.registerFilter(ContentName.fromURI("/"), this);
			int _flags = PrefixRegistrationManager.CCN_FORW_ACTIVE | PrefixRegistrationManager.CCN_FORW_LAST; //We only want to search for new mappings if no other mappings exist.
			incomingCCNHandle.getNetworkManager().setInterestFilter(incomingCCNHandle, ContentName.fromURI("/"), this, _flags);
			
			while(true)
			{
				try {
					//System.out.println("Waiting to remove expired mappings.");
					Thread.sleep(30000L);
					
					//clear expired sessions to allow periodic refresh.
					synchronized(Mappings)
					{
						
						Enumeration<String> e = Mappings.keys();
						//System.out.println("Looping through all current mappings");
						while(e.hasMoreElements())
						{
							String key = e.nextElement();
							Mapping map = Mappings.get(key);
							if(map != null && map.isExpired())
							{
								System.out.println("Removing "+key);
								map.unRegisterAll();
								Mappings.remove(key);
								//System.out.println("Removed "+key);
							}
							
						}
						
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (MalformedContentNameStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public boolean handleInterest(Interest intrst) {
		if(intrst.name().count()>=1){
			
			String _lookup = intrst.name().stringComponent(0);

			synchronized(Mappings) //only one at a time may touch the mappings.
			{
				if(Mappings.get(_lookup) != null && Mappings.get(_lookup).isExpired())
				{	
					Mapping tMap = Mappings.remove(_lookup);
					if(tMap != null)
						tMap.unRegisterAll();
				}
				
				if(Mappings.get(_lookup) == null)
				{
					System.out.println("Find mapping for "+_lookup);
					lookupMapping(_lookup);
				}
				else
				{
					System.out.println("Mapping (possibly empty) for "+_lookup+" already exists!");
				}
			}
			
		}
		return false;
	}

	private void lookupMapping(String _lookup) {
		
		Mapping map = new Mapping(_lookup);
		Mappings.put(_lookup, map);
		
		Lookup lookup;
		try {
			lookup = new Lookup(_lookup, Type.TXT);
		
			if(_resolver != null)
			{
				lookup.setResolver(_resolver);
			}
			
			Record records[] = lookup.run();
			
			if(lookup.getResult() == Lookup.SUCCESSFUL)
			{
				for(int i = 0; i< records.length; i++)
					if(records[i] instanceof TXTRecord)
				{
					TXTRecord txtRecord = (TXTRecord) records[i];
										
					
					for(@SuppressWarnings("rawtypes")
					Iterator rData = txtRecord.getStrings().iterator(); rData.hasNext(); /*empty*/)
					{
						String rDataElem = (String) rData.next();
						String txtArr [] =  rDataElem.split(" ");
						if(txtArr.length >= 2 && txtArr[0].equals("v=ndn"))
						{	
							System.out.println("Accepting " + rDataElem);
							for(int j = 1; j < txtArr.length; j++)
							{
								System.out.println("Mapping from " + _lookup + " to " +txtArr[j]);
								map.add(txtArr[j]);
							}
						}
						else
						{
							System.out.println("Ignoring " + rDataElem);
						}
					}
				}
			}
		} catch (TextParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		map.postponeExpiry(300);
		
	}

}
