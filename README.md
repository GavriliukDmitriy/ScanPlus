# ScanPlus
Application for scanning and working in a database (version for Officemag)

This application uses the following functionality: 
- reading string information from the barcode when capturing the screen using the device's camera using the Zxing library;
- processing the resulting string representation of the barcode in the SQLite database; 
- depending on the consistency of the database file, the operating mode is determined programmatically, without the user's participation,
(displaying the product corresponding to the barcode/displaying with a change in the quantity of the selected product);
- also for the case when the barcode in the database file is not found, 
the ability to view the product card on the site by matching the read barcode is implemented officemag.ru. 

In principle, this is a full-fledged terminal for working with the barcode in the store. 
But the peculiarity of this project is its attachment to the DB file format (a sample is presented in the assets) and the above site. 

So, if you have a desire to adapt this solution to your conditions - modify the code snippets with the file name, SQL overgrowth, site URL and use it.
