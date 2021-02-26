const express = require('express');
const app = express();

app.listen(7777, () => {
	console.log('http://localhost:7777/');
});

app.use(express.static(__dirname));