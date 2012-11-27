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

import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;
import org.xbill.DNS.SimpleResolver;

public class Rename {

	
	/**
	 * @param args
	 */
	public static void StartMessage()
	{
		System.out.println("Please run with the following arguments:");
		System.out.println("\t\"in\" or \"out\"; to define whether you need to rename incoming, or outgoing Interests and the respective contentObjects."); 
		System.out.println("\t <user-registered name>; the user-registered name (to be) appended to the routeable name.");
		System.out.println("\t <routable name>; the globally routeable name to which Interest are, or will be, sent to.");
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		ContentName nameA, nameB;
		// TODO Auto-generated method stub
		if(args.length < 1 || args.length > 3)
		{
			StartMessage();	
		}
		
		if(!(args[0].equals("dns")||args[0].equals("in")||args[0].equals("out")))
		{
			StartMessage();
		}
		
		if(args[0].equals("dns") && args.length != 1 && args.length !=2)
		{
			StartMessage();
		}
		
		if(args[0].equals("in") && args.length != 2)
		{
			StartMessage();
		}
		
		if(args[0].equals("out") && args.length != 3)
		{
			StartMessage();
		}
		
		
		
		try {
							
			//System.out.println("Good boy, right parameters");
			if(args[0].equals("dns"))
			{
				System.out.println("Let's manage all your mappings using DNS");
				if(args.length == 1)
				{
					new RenameManager();
				}
				if(args.length == 2)
				{
					SimpleResolver rslvr = new SimpleResolver(args[1]);
					new RenameManager(rslvr);
				}
			}
			
			if(args[0].equals("out"))
			{
				nameA = ContentName.fromURI(args[1]);
				nameB = ContentName.fromURI(args[2]);
				new RenameOutgoing(nameA, nameB);
			}
			if(args[0].equals("in"))
			{
				nameA = ContentName.fromURI(args[1]);
				new RenameIncoming(nameA);
			}
			
		} catch (MalformedContentNameStringException e) {
			StartMessage();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		
			
	}

}
