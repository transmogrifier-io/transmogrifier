import asyncio
import json
import urllib

import aiofiles
import requests
import os


async def read_local_file_node(file_path):
    try:
        async with aiofiles.open(file_path, mode='r', encoding='utf-8') as file:
            return await file.read()
    except Exception as e:  # TODO
        raise e  # You can handle the error here as needed


async def read_local_file_web(file_url):
    try:
        response = requests.get(file_url)
        if response.status_code == 200:
            return response.text
        else:
            raise Exception('Error reading file')

    except Exception as e:
        raise e  # You can handle the error here as needed


# left Android implementation for the user
# left iOS implementation for the user


async def read_file(file_path):
    try:
        if os.path.exists(file_path):
            data = await read_local_file_node(file_path)
        else:  # TODO as android/ios are implemented
            # Running in a web browser-like environment
            data = await read_local_file_web(file_path)
        return data
    except Exception as e:
        raise e  # You can handle the error here as needed


async def read_url(url):
    try:
        response = requests.get(url)
        if response.status_code == 200:
            return response.text
        else:
            raise Exception(f"Failed to read URL \"{url}\": HTTP status code {response.status}")

    except Exception as e:
        raise e  # You can handle the error here as needed


async def read_URL_or_File(path):
    try:
        if os.path.exists(path):
            data = await read_file(path)
        else:  # TODO as android/ios are implemented
            # Running in a web browser-like environment
            data = await read_url(path)
        return data
    except Exception as e:
        raise e  # You can handle the error here as needed


async def write_local_file_node(file_path, append, data):
    try:
        mode = 'a' if append else 'w'

        with open(file_path, mode, encoding='utf-8') as file:
            if append:
                file.write("\n" + data)
            else:
                file.write(data)

        return None  # No need to return a value, just resolve with None in Python

    except Exception as e:
        raise e  # You can handle the error here as needed


async def write_local_file_web(filePath, append, data):
    url = filePath  # Assuming the filePath is a URL
    headers = {'Content-Type': 'text/plain'}

    try:
        if append:
            response = requests.patch(url, headers=headers, data=data)
        else:
            response = requests.put(url, headers=headers, data=data)

        if response.status_code == 200:
            print(response.text)
            return True
        else:
            raise Exception('Error writing file')

    except Exception as e:
        raise e


# left Android implementation for the user
# left iOS implementation for the user


async def write_file(params, data):
    try:
        file_path = params.get('path', '')
        append = params.get('append', False)

        if os.path.exists(file_path):
            await write_local_file_node(file_path, append, data)
        else:  # TODO as android/ios are implemented
            print('here')
            await write_local_file_web(file_path, append, data)


    except Exception as e:
        raise e  # You can handle the error here as needed


async def write_url(url, data):
    try:
        # Parse the URL
        url_parts = urllib.parse.urlparse(url)
        hostname = url_parts.hostname
        path = url_parts.path

        # Prepare the POST data
        post_data = json.dumps(data)
        headers = {
            'Content-Type': 'application/json',
            'Content-Length': str(len(post_data))
        }

        response = requests.post(url, headers=headers, data=post_data)
        if response.status_code != 200:
            raise Exception(f"Failed to post data \"{hostname + path}\": HTTP status code {response.status_code}")

        return response.text
    except Exception as e:
        raise e  # You can handle the error here as needed


async def test():
    params = 'https://httpbin.org/post'
    data_to_write = "'data':'This is the data to write'"

    try:
        await write_url(params, data_to_write)
        print("File written successfully.")
    except Exception as e:
        print(f"An error occurred: {e}")

asyncio.run(test())