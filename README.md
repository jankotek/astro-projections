
Set of astronomical projections (from plane to sphere) and WCS (World Coordinate System). This is high performance library which uses matrix transformations rather then trigonometrical operations. It also recycles vector instances to reduce GC trashing. 

This code was developed by NASA to compose mosaic images in [Skyview app](http://skyview.gsfc.nasa.gov/). This fork moves projections and WCS into independent small package. Original implementation was also not thread-safe, and only suitable for batch processing. 

![MELL](http://skyview.gsfc.nasa.gov/blog/wp-content/uploads/2009/12/mell_rgb_450.jpg)
