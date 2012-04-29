/*
    Copyright (c) 2007, 2012 Paul Richards <paul.richards@gmail.com>

    Permission to use, copy, modify, and/or distribute this software for any
    purpose with or without fee is hereby granted, provided that the above
    copyright notice and this permission notice appear in all copies.

    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

#include <upnp/upnp.h>
#include <iostream>
#include <string>

namespace {
    const std::string SEARCH_TARGET = "urn:schemas-upnp-org:device:InternetGatewayDevice:1";
    UpnpClient_Handle handle;

    int CallbackFunction(Upnp_EventType eventType, void* event, void* cookie)
    {
        try {
            assert(cookie == NULL);
            switch (eventType) {
                case UPNP_DISCOVERY_ADVERTISEMENT_ALIVE:
                case UPNP_DISCOVERY_SEARCH_RESULT:
                {
                    Upnp_Discovery* discoveryEvent = static_cast<Upnp_Discovery*>(event);
                    std::cout << discoveryEvent->Location << std::endl;
                    std::cout << discoveryEvent->DeviceId << std::endl;
                    std::cout << discoveryEvent->DeviceType << std::endl;
                    std::cout << discoveryEvent->ServiceType << std::endl;

                    char* buffer = NULL;
                    char contentType[LINE_SIZE] = {0};
                    if (UpnpDownloadUrlItem(discoveryEvent->Location, &buffer, contentType) != UPNP_E_SUCCESS) {
                        throw std::string("UpnpDownloadUrlItem() failed");
                    }
                    std::cout << buffer << std::endl;
                    free(buffer);

                    IXML_Document* descriptionDocument = NULL;
                    if (UpnpDownloadXmlDoc(discoveryEvent->Location, &descriptionDocument) != UPNP_E_SUCCESS) {
                        throw std::string("Upnp_DownloadXmlDoc() failed");
                    }
                    if (descriptionDocument) {
                        ixmlDocument_free(descriptionDocument);
                    }

                    break;
                }
                default:
                    std::cout << "Unsupported Upnp_EventType: " << eventType << std::endl;
            }

            return 0;
        } catch (const std::string& error) {
            std::cerr << error << std::endl;
            exit(EXIT_FAILURE);
        }
    }
}

int main(void)
{
    try {
        if (UpnpInit(NULL, 0) != UPNP_E_SUCCESS) {
            throw std::string("UpnpInit() failed");
        }
        if (UpnpRegisterClient(CallbackFunction, NULL, &handle) != UPNP_E_SUCCESS) {
            throw std::string("UpnpRegisterClient() failed");
        }
        if (UpnpSearchAsync(handle, 5, SEARCH_TARGET.c_str(), NULL) != UPNP_E_SUCCESS) {
            throw std::string("UpnpSearchAsync() failed");
        }

    } catch (const std::string& error) {
        std::cerr << error << std::endl;
        UpnpFinish();
        return EXIT_FAILURE;
    }
    UpnpFinish();
    return EXIT_SUCCESS;
}

