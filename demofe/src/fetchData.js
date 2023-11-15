
export const postAPI = async (url, post) => {
    try {
        const response = await fetch(url, {
            method: "POST",
            body: post,
        });
        if (!response.ok) {
            throw new Error('Error in service');
        }
        const rs = await response.json();
        return rs
    } catch (e) {
        console.log(e);
    }
};
export const getAPI = async (url) => {
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('Error in service');
        }


        const rs = await response.json();

        return rs
    } catch (e) {
        console.log(e);
    }

};

export const patchAPI = async (url, post) => {
    try {
        const response = await fetch(url, {
            method: "PATCH",
            body: post,
        });
        if (!response.ok) {
            throw new Error('Error in service');
        }
        const rs = await response.json();
        return rs
    } catch (e) {
        console.log(e);
    }
};
export const putAPI = async (url, post) => {
    try {
        const response = await fetch(url, {
            method: "PUT",
            body: post,
        });
        if (!response.ok) {
            throw new Error('Error in service');
        }
        const rs = await response.json();
        return rs
    } catch (e) {
        console.log(e);
    }
};
export const deleteAPI = async (url) => {
    try {
        const response = await fetch(url, {
            method: "DELETE",
        
        });
        if (!response.ok) {
            throw new Error('Error in service');
        }
        const rs = await response.json();
        return rs
    } catch (e) {
        console.log(e);
    }
};
