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
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;


public class Mapping {

	private static final int DEFAULT_EXPIRY_TIME = 300000; //ms, thus 5 minutes
	private long _expiry;
	private String _name;
	private Hashtable<String, RenameOutgoing> destinations;
	
	public Mapping(String tName) {
		destinations = new Hashtable<String, RenameOutgoing>();
		_name = tName;
		
		_expiry = (new Date()).getTime() + DEFAULT_EXPIRY_TIME;
		
	}

	public boolean isExpired() {
		// TODO Auto-generated method stub
		return _expiry < (new Date()).getTime() ;
	}

	
	public void unRegisterAll() {
		// TODO Auto-generated method stub
		Enumeration<String> e = destinations.keys();
		while(e.hasMoreElements())
		{
			String key = e.nextElement();
			RenameOutgoing ro = destinations.get(key);
			ro.unRegister();
			destinations.remove(key);
		}
	}

	public void add(String destination){
		// TODO Auto-generated method stub
		try {
			destinations.put(destination, new RenameOutgoing(ContentName.fromURI("/"+_name), ContentName.fromURI(destination)));
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void postponeExpiry(int tSeconds) {
		// TODO Auto-generated method stub
		
	}

	

	
}
