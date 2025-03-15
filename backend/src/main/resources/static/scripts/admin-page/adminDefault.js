
import { fetchData } from '../services/fetchService.js'
import { getPostsTBodyTemplate, getUsersTBodyTemplate } from './adminTemplates.js'

// CSRF token and header for secure requests
const CSRF_TOKEN = document.querySelector('meta[name="_csrf"]').content
const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]').content


const setPostsEvents = () => {
    // Handle post card clicks to navigate to the post's page
    const posts = [...document.querySelectorAll('[data-post-id]')]
    posts.forEach(post => {
        const postId = post.getAttribute('data-post-id')
        post.addEventListener('click', (e) => {
            location.href = `/posts/${postId}`
        })

        post.querySelector('[data-admin-command="post-delete"]')?.addEventListener('click', (e) => {
            e.stopPropagation()
            console.log('click')

            const confirmDelete = confirm('¿Estás seguro de que deseas eliminar este post?')
            if (confirmDelete) {
                const endpoint = `/api/post/${postId}`
                fetchData(endpoint, "DELETE", { cacheData: false })
                .then(() => fetchPostsTable(false))
            }
        })
    })
}


const setUsersEvents = () => {
    // Delete user buttons
    const $deleteUserButtons = [...document.querySelectorAll('[data-admin-command="user-delete"]')]
    $deleteUserButtons.forEach($deleteUserButton => {
        $deleteUserButton.addEventListener('click', (e) => {
            e.stopPropagation()

            const confirmDelete = confirm('¿Estás seguro de que deseas eliminar este usuario?')
            const uid = e.target.getAttribute('data-user-id')

            if (confirmDelete) {
                fetch(`/api/admin/user/${uid}`, {
                    method: 'DELETE',
                    headers: {
                        [CSRF_HEADER]: CSRF_TOKEN
                    }
                })
                .then(() => fetchUsersTable(false))
            }
        })
    })
}


let limitUsers = 5
const fetchUsersTable = async (incLimit=true) => {
    if (incLimit) limitUsers += 5
    const res = await fetch(`/api/admin/users?offset=0&limit=${limitUsers}`)
    const resJson = await res.json()
    const data = resJson.data

    document.querySelector('[data-replace="admin-users"]').outerHTML = getUsersTBodyTemplate(data.users, data.total, data.maxReached)
    setUserButtonListener()
    setUsersEvents()
}

let limitPosts = 5
const fetchPostsTable = async (incLimit=true) => {
    if (incLimit) limitPosts += 5
    const res = await fetch(`/api/admin/posts?offset=0&limit=${limitPosts}`)
    const resJson = await res.json()
    const data = resJson.data

    document.querySelector('[data-replace="admin-posts"]').outerHTML = getPostsTBodyTemplate(data.posts, data.total, data.maxReached)
    setPostButtonListener()
    setPostsEvents()
}

const setUserButtonListener = () => {
    const $loadMoreUsers = document.querySelector('[data-admin-command="load-more-users"]')
    $loadMoreUsers?.addEventListener('click', ()=>{
        $loadMoreUsers.innerText = 'Cargando...'
        fetchUsersTable()
    })
}

const setPostButtonListener = () => {
    const $loadMorePosts = document.querySelector('[data-admin-command="load-more-posts"]')
    $loadMorePosts?.addEventListener('click', ()=>{
        $loadMorePosts.innerText = 'Cargando...'
        fetchPostsTable()
    })
}

// ---------------------------------------------------------------------------------------------------------

fetchUsersTable(false)
fetchPostsTable(false)