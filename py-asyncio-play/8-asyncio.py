
import aiohttp
import asyncio
import time

host = 'http://example.com'
url_todo = {'/', '/1', '/2', '/3', '/4', '/5', '/6', '/7', '/8', '/9'}

loop = asyncio.get_event_loop()


async def fetch(url):
    async with aiohttp.ClientSession(loop=loop) as session:
        async with session.get(url) as response:
            response = await response.read()
            return response


if __name__ == "__main__":
    start = time.time()
    tasks = [fetch(host + url) for url in url_todo]
    loop.run_until_complete(asyncio.gather(*tasks))
    
    print(time.time() - start)
