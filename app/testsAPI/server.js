const jsonServer = require('json-server')
const server = jsonServer.create()
const router = jsonServer.router('db.json')
const middlewares = jsonServer.defaults()


server.use(middlewares)
server.use(jsonServer.rewriter({
  '/file/upload': '/file',
  '/video/:id': '/video/:id',
  '/oauth/token': '/oauth'
}))

server.use(jsonServer.bodyParser)
server.use((req, res, next) => {
  if (req.method === 'POST') {
 	req.method = 'GET';
  	req.query = req.body;
  }
  if (req.method === 'DELETE') {
 	req.method = 'GET';
  	req.query = req.body;
  }
  // Continue to JSON Server router
  next()
})

server.use(router)
server.listen(3000, () => {
  console.log('JSON Server is running')
})
