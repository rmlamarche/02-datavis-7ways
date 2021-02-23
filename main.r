# Source for learning because I don't really know R: https://www.r-graph-gallery.com/320-the-basis-of-bubble-plot.html
# Libraries
library(ggplot2)
library(dplyr)

data = read.csv("cars-sample.csv", header = TRUE)

# Most basic bubble plot
ggplot(data, aes(x=Weight, y=MPG, size = Weight, color=Manufacturer)) +
  geom_point(alpha=0.7)

