# RangeSeekBar

The missing view for android. 

[![](https://jitpack.io/v/Innovattic/range-seek-bar.svg)](https://jitpack.io/#Innovattic/range-seek-bar) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-RangeSeekBar-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/7063)

- [Screenshot](#screenshot)
- [Usage](#usage)
- [Attributes](#attributes)

### Screenshot

<img src="/screenshots/sample.png" width="400px" />

### Usage
First add jitpack to your projects build.gradle file

```gradle
allprojects {
   	repositories {
   		...
   		maven { url "https://jitpack.io" }
   	}
}
```

Then add the dependency in your android app module's `build.gradle` file.

If you are upgrading from a previous version, please take a look at [changelogs](#changelogs) to make sure nothing will break.

```gradle
dependencies {
    implementation 'com.github.Innovattic:range-seek-bar:v1.0.5'
}
```

Then use the view in your layouts:

```xml
<com.innovattic.rangeseekbar.RangeSeekBar
    android:id="@+id/rangeSeekBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@color/colorAccent" />
```


### Attributes

You can change view attributes directly from your layout's xml file or in your java/kotlin code:

| Variable                   | XML Attribute                | Type      | Description                                                                            |
| :------------------------- | :--------------------------- | :-------- | :--------------------------------------------------------------------------------------|
| trackColor                 | rsb_trackColor               | color     | Color of horizontal track                                                              |
| trackSelectedColor         | rsb_trackSelectedColor       | color     | Color of the selected range of horizontal track                                        |
| trackThickness             | rsb_trackThickness           | dimension | The thickness of the horizontal track                                                  |
| trackSelectedThickness     | rsb_trackSelectedThickness   | dimension | The thickness of the selected range of horizontal track                                |
| sidePadding                | rsb_sidePadding              | dimension | Side padding for view, by default 16dp on the left and right                           |
| touchRadius                | rsb_touchRadius              | dimension | The acceptable touch radius around thumbs in pixels                                    |
| minThumbDrawable           | rsb_minThumbDrawable         | reference | The drawable to draw min thumb with                                                    |
| maxThumbDrawable           | rsb_maxThumbDrawable         | reference | The drawable to draw max thumb with                                                    |
| minRange                   | rsb_minRange                 | integer   | The minimum range to be selected. It should at least be 1                              |
| max                        | rsb_max                      | integer   | The maximum value of thumbs which can also be considered as the maximum possible range |
| minThumbOffset             | rsb_minThumbOffsetVertical   | dimension | Vertical offset of min thumb                                                           |
| minThumbOffset             | rsb_minThumbOffsetHorizontal | dimension | Horizontal offset of min thumb                                                         |
| maxThumbOffset             | rsb_maxThumbOffsetVertical   | dimension | Vertical offset of max thumb                                                           |
| maxThumbOffset             | rsb_maxThumbOffsetHorizontal | dimension | Horizontal offset of max thumb                                                         |
| trackRoundedCaps           | rsb_trackRoundedCaps         | boolean   | If the track should have rounded caps.                                                 |
| trackSelectedRoundedCaps   | rsb_trackSelectedRoundedCaps | boolean   | If the selected range track should have rounded caps.                                  |

### Changelogs

- v1.0.5: Converted thumb position offset attributes to dimensions instead of integer
- v1.0.4: Added option to make tracks have rounded caps
- v1.0.3: AndroidX support
- v1.0.2: Added the option to offset the thumb drawables.
- v1.0.1: Made the `RangeSeekBar` class, `open`
