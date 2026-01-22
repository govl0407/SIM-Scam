import axios from "axios"

export async function sendChat(message) {
    const res = await axios.post("http://localhost:8080/test/chat", {
        message: message
    })
    return res.data
}
