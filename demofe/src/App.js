
import React, { useState } from "react";


import { MapContainer, TileLayer, Marker, Polyline } from "react-leaflet";

import { useRef } from "react";
import { blueIcon, greenIcon } from "./Icon";
import "leaflet/dist/leaflet.css";
function App() {




  const [center, setCenter] = useState({ lat: 10.7769, lng: 106.7009 });
  const mapRef = useRef();
  const [searchData, setSearchData] = useState(null)
  const [path, setPath] = useState([])

  const onInputChange = (e) => {
    const { name, value } = e.target;
    setSearchData({
      ...searchData,
      [name]: value,
    });
  }

  const searchSubmit = async () => {
    const resdata = await fetch("http://localhost:8080/map/api/search?source=1924760081&destination=7234432185")
      .then(response => {

        if (!response.ok) {
          throw new Error('Có lỗi khi tải dữ liệu từ API');
        }

        return response.json();
      })

      .catch(error => {

        console.error(error);
      });

    console.log(resdata);
    setPath(resdata)
  }

  console.log(path);
  return (
    <div >
      <div className="header">
        <label htmlFor="">Source</label>
        <input type="text" name="source" onChange={onInputChange} />
        <label htmlFor="">Destination</label>
        <input type="text" name="destination" onChange={onInputChange} />
        <button onClick={searchSubmit}>find</button>
      </div>
      <MapContainer
        style={{ height: '500px', width: '80%', margin: '100px auto' }} center={[center.lat, center.lng]} zoom={9} >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />

        {path.length && <>
          <Marker position={[path[0].lat, path[0].lon]} icon={greenIcon} />
          <Marker position={[path[path.length - 1].lat, path[path.length - 1].lon]} icon={greenIcon} />
          <Polyline positions={path} pathOptions={{
            color: 'blue',
            weight: 6,
          }} />
        </>}


      </MapContainer>
    </div>
  );




}

export default App;
