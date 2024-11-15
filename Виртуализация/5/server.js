const http = require('http')
const PORT = 3000

const handleRequest = (request, response) => {
    console.log(Received request from ${request.connection.remoteAddress})
    response.writeHead(200)
    response.end('Server is working\n')
}

const server = http.createServer(handleRequest)
server.listen(PORT, () => {
    console.log(Server listening on port ${PORT})
})
