
const getCookie = (name /*: string */) /*: string | null */ => {
    return document.cookie.split(';')
        .map(cookie => cookie.trim())
        .filter(cookie => cookie.startsWith(name + '='))
        .map(cookie => cookie.substring(name.length + 1))
        .find(cookie => cookie !== undefined && cookie !== null) || null;
}

export { getCookie };