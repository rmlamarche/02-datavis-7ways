import pandas as pd
import plotly.express as px

df = pd.read_csv('cars-sample.csv')

fig = px.scatter(df, x="Weight", y="MPG", size="Weight", color="Manufacturer", hover_name="Car", size_max=40)
fig.show()