# solulabtest

- You can directly download debug APK from "APK" folder.
- Or you can checkout this sourcecode & build.
 
- Please tap current location Icon on top right corner to point on current location then start tracking by tapping FAB.
- Currently milestone is set to 50 meters.
- You can change it to anything from Config.java file. [You can also find other setting here]


=> Tab 1
	- It will show map with curent location button.
	- After tracking started you will see black TextView on top left corner with current LatLong & distance
		- Due to some approximate locations our LatLong will be different each time even on same location. [Sometimes the reason is our mobile's network signals]
		
=> Tab 2
	- It will only show Total travelled distance in meters [From app install to present]
	
=> Tab 3
	- It will show the added records on each milestone.
	- List will get updated on each milestone automatically.
	- List item shows
		- Record ID
		- Latitude
		- Longitude
		- Record created DateTime