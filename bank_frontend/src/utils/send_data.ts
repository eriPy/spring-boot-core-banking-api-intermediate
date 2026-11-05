export const createJson = (method: string, body?: Record<string, any>) => {
    return {
        method: method,
        headers: {"Content-Type": "application/json"},
        body: body ? JSON.stringify(body) : undefined
    }
}

export const addJwt = (json: Record<any, any>, jwt: string) => {
    json.headers["Authorization"] = `Bearer ${jwt}`
    return json;
}