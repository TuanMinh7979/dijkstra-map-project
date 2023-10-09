
import React, { useEffect, useState } from "react";


import { Marker, Polyline } from "react-leaflet";

import { useRef } from "react";
import Map from "./Map";
import "leaflet/dist/leaflet.css";
import "./App.css";
import { greenIcon } from "./Icon"
import SearchBar from "./SearchBar";
function App() {





  const [center, setCenter] = useState({ lat: 10.7769, lng: 106.7009 });
  const mapRef = useRef();
  const [searchData, setSearchData] = useState(null)
  const [path, setPath] = useState([])

  const [activeMap, setActiveMap] = useState("")
  const [mapList, setMapList] = useState([])
  const [places, setPlaces]= useState([])
  const [appName, setAppName] = useState("abc")
  const onInputChange = (e) => {
    const { name, value } = e.target;
    setSearchData({
      ...searchData,
      [name]: value,
    });
  }

  const fetchMaps = async () => {
    const rs = await fetch("http://localhost:8080/maps")
    const rsdata = await rs.json()
    setMapList(rsdata)
    setActiveMap(rsdata[0])
  }

  const fetchCurMap = async () => {
    if (activeMap) {
      const rs = await fetch(`http://localhost:8080/map/${activeMap}`)
      const resdata = await rs.json();
      setCenter({ lng: resdata.lng, lat: resdata.lat })
      const rs1 = await fetch(`http://localhost:8080/places`)
      const resdata1 = await rs1.json();
      setPlaces(resdata1)

    }


  }

  useEffect(() => {
    fetchMaps();
  }, [])
  useEffect(() => {
    fetchCurMap()


  }, [activeMap])


  

  const searchSubmit = async () => {
    const resdata = await fetch("http://localhost:8080/map/api/search?source=5247369420&destination=5257234327")
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



  return (
    <div >
      <div className="header">


        <div className="left">
          {/* <label htmlFor="">Source</label>
          <input type="text" name="source" onChange={onInputChange} /> */}

          <SearchBar data={places
          }></SearchBar>

          <SearchBar data={
            places
          }></SearchBar>

          <button onClick={searchSubmit}>find</button>
        </div>

        <div className="right">
          <select onChange={(e) => setActiveMap(e.target.value)} name="" id="">
            {mapList.length > 0 && mapList.map(el => <option value={el}>{el}</option>)}
          </select>
        </div>


      </div>
      <Map
        styles={{ height: '500px', width: '80%', margin: '100px auto' }} center={[center.lat, center.lng]} zoom={16} >


        {path.length && <>
          <Marker position={[path[0].lat, path[0].lon]} icon={greenIcon} />
          <Marker position={[path[path.length - 1].lat, path[path.length - 1].lon]} icon={greenIcon} />
          <Polyline positions={path} pathOptions={{
            color: 'blue',
            weight: 6,
          }} />
        </>}


      </Map>
    </div>
  );




}

export default App;
